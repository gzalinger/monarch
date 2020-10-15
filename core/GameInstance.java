// An instance of a single scenario...this object also acts as the game logic controller

package core;
import java.awt.geom.Point2D.Double;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;


public class GameInstance implements Serializable {

	private static final double MONARCH_BASE_MOVE_SPEED = 60.0; // in map coords
	public static final int NODE_CAPTURE_COST = 5;
	public static final int FARM_UNLOCK_LEVEL = 2; // level palace must be before farms can be built
	public static final int DEFENSE_PER_SOLDIER = 3;
	public static final double CAPTAIN_STRENGTH_BONUS = 1.2;
	public static final double NIGHT_TO_DAY_RATIO = 0.666; // length of night compared to day
	private static final double MARGINAL_EQUIP_COST_WORKER = 0.5; // how much more each costs than the last
	private static final double MARGINAL_EQUIP_COST_SOLDIER = 0.25;
	private static final int MAX_SOLDIER_EQUIP_COST = 15;
	private static final int[] purseSizes = new int[]{20, 25, 35, 45, 60}; // limits on what the monarch can carry
	private static final int[] retinueLimits = new int[]{3, 4, 5, 6, 8};
	private static final double[] monarchMoveSpeedBonuses = new double[]{0.0, 0.0, 0.05, 0.1, 0.15}; // confered by capital level
	private static final int CITY_EXCLUSION_DISTANCE = 2; // distance that must be between cities
	private GameState state; // the model in MVC
	private Double monarchMovementDirection;


	public GameInstance(DifficultyLevel dl, int mapDifficulty, int mapSize) {
		//state = new GameState(Map.getDefaultMap(), DifficultyLevel.getDefault());
		state = new GameState(this, MapGen.createMap(mapDifficulty, mapSize), dl);
		monarchMovementDirection = new Double(0, 0);
		init();
	}

	// =================================

	private void init() {
		// Set up all initial game stuff (e.g. starting money and population, capital city level)
		MapNode root = state.getMap().getRootNode();
		state.exploreNode(root);
		state.captureNode(root, true);
		state.setMonarchLocation(root);
		Settlement capital = new Capital(2);
		capital.addGold(20);
		state.addSettlement(capital, root);
		for (int i = 0; i < 5; i++) {
			state.addSubjectToNodePopulation(root, SubjectType.IDLE, this);
		}

		state.increaseDangerLevel(1.0);

		// create the peddlers:
		for (MapNode node: state.getMap().getPeddlerLocations()) {
			Peddler peddler = new Peddler();
			peddler.addGold(2);
			state.addSettlement(peddler, node);
		}

		// Add enemy strongholds:
		for (MapNode leaf: state.getMap().getLeaves()) {
			state.addStronghold(leaf, new Stronghold(leaf));
		}
		// Intermediate ones:
		for (MapNode node: state.getMap().getStrongholdLocations()) {
			state.addStronghold(node, new Stronghold(node));
		}

		// Add resources:
		for (MapNode node: state.getMap().getNodeList()) {
			if (state.getStrongholdAt(node) != null) {
				continue;
			}

			// add some population to uncaptured nodes:
			if (!state.isNodeCaptured(node)) {
				if (Math.random() < 0.5) {
					state.addSubjectToNodePopulation(node, SubjectType.IDLE, this);
					// some of them have 2 pop:
					if (Math.random() < 0.25) {
						state.addSubjectToNodePopulation(node, SubjectType.IDLE, this);
					}
				}
			}
		}

		state.getHousingTracker().recalculate(this);
	}

	// =================================

	public void update(double progress) {
		// This is what the GameEngine calls to advance the game. Progress is the percantage of day or night that has passed.
		if (state.getDayNightTime() + progress > 1.0) {
			progress = 1.0 - state.getDayNightTime();
		}

		// settlements only progress during the day:
		if (isDaytime()) {
			for (Settlement s: state.getAllSettlements()) {
				s.updateSettlement(this, progress);
			}
			for (Wall w: state.getAllWalls()) {
				w.update(progress, this);
			}
		}
		else {
			// Update attacks:
			LinkedList<FrontierAttack> attacksCopy = new LinkedList<FrontierAttack>();
			attacksCopy.addAll(state.getFrontierAttacks());
			for (FrontierAttack attack: attacksCopy) {
				MapNode target = attack.getTargetNode();
				// Some attacks may have already finished...skip them
				if (!state.isNodeCaptured(target)) {
					continue;
				}
				
				attack.update(progress);
				
				if (attack.getCurrentStrength() > getDefenseRating(target)) {
					// lose this node:
					loseNode(target);

					// continue attack to parent:
					FrontierAttack parentAttack = getAttackAtNode(target.getParent());
					int remainingStrength = (int)Math.round(attack.getTotalDanger() - attack.getCurrentStrength());
					if (parentAttack == null) {
						// start a new attack with remaining strength:
						state.addFrontierAttack(new FrontierAttack(target.getParent(), remainingStrength));
					}
					else {
						parentAttack.addTotalDanger(remainingStrength);
					}
				}
			}
		}

		// sorties;
		LinkedList<Sortie> sortieList = new LinkedList<Sortie>();
		sortieList.addAll(state.getSorties()); // copy to avoid concurrent mod errors
		for (Sortie s: sortieList) {
			s.update(progress, this);
		}

		// update time:
		if (state.updateDayNightTime(progress)) {
			// A day or night has ended.
			if (isDaytime()) {
				onDayBegin();
			}
			else {
				onNightBegin();
			}
		}

		// movement:
		if (monarchMovementDirection.getX() != 0.0 || monarchMovementDirection.getY() != 0.0) {
			updateMonarchMovement(progress);
		}
	}

	// =================================

	private void loseNode(MapNode target) {
		// lose it to an attack
		if (target == state.getMap().getRootNode()) {
			gameOver(false);
			return;
		}

		if (state.getSettlementAt(target) != null) {
			state.removeSettlementAt(target);
		}
		if (state.getWallAtNode(target) != null) {
			state.removeWallAt(target);
		}

		state.uncaptureNode(target);
		// Move all soldiers and captains back:
		for (SubjectType st: SubjectType.values()) {
			if (!st.isCivilian()) {
				while (state.getPopulationAtNode(target, st) > 0) {
					state.subtractPopulationAtNode(target, st, this);
					state.addSubjectToNodePopulation(target.getParent(), st, this);
				}
			}
		}

		// kill some civilians:
		int totalCivilians = 0;
		for (SubjectType st: SubjectType.values()) {
			if (!st.isCivilian()) {
				continue;
			}
			totalCivilians += state.getPopulationAtNode(target, st);
		}
		if (totalCivilians > 0) {
			int deaths = Math.max(1, totalCivilians / 3);
			for (int i = 0; i < deaths; i++) {
				state.subtractPopulationAtNode(target, Population.getWeightedRandomSubject(target, this), this);
			}
		}
		// move remaining civilians back to parent node:
		for (SubjectType st: SubjectType.values()) {
			while (state.getPopulationAtNode(target, st) > 0) {
				state.subtractPopulationAtNode(target, st, this);
				state.addSubjectToNodePopulation(target.getParent(), st, this);
			}
		}


		// Look for downstream nodes that have been cut off as a result:
		loseAllDownstreamNodes(target);

		// update housing:
		state.getHousingTracker().recalculate(this);
	}

	// =================================

	private void loseAllDownstreamNodes(MapNode target) {
		if (isNodeCaptured(target)) {
			if (state.getSettlementAt(target) != null) {
				state.removeSettlementAt(target);
			}
			state.uncaptureNode(target);
			// kill all subjects, including soldiers:
			state.clearPopulationAtNode(target);
		}
		for (MapNode child: target.getChildren()) {
			loseAllDownstreamNodes(child);
		}
	}

	// =================================

	private void onDayBegin() {
		// Generate daily gold at palace:
		Settlement palace = getSettlementAt(state.getMap().getRootNode());
		int dailyGold = 2;
		if (palace.getGold() + dailyGold > palace.getMaxGold()) {
			dailyGold = palace.getMaxGold() - palace.getGold();
		}
		palace.addGold(dailyGold);

		// Increase danger level:
		state.increaseDangerLevel(state.getDifficultyLevel().getDailyDangerIncrease(state.getDayCount()));

		state.clearFrontierAttacks();
	}

	// =================================

	private void onNightBegin() {
		// end all existing sorties:
		state.clearSorties();

		// Generate the attacks for tonight:
		for (MapNode node: state.getFrontiers()) {
			if (!hasDownstreamStronghold(node)) {
				continue;
			}
			double danger = state.getDangerLevel();
			// random variance:
			danger = danger * (0.9 + Math.random()*0.2);

			state.addFrontierAttack(new FrontierAttack(node, (int)Math.round(danger)));
		}
	}

	// =================================

	private boolean hasDownstreamStronghold(MapNode node) {
		for (MapNode child: node.getChildren()) {
			// If this child is captured (can happen at forks), stop looking at it b/c attacks from anything
			// downstream will hit this child instead:
			if (state.isNodeCaptured(child)) {
				continue;
			}
			if (state.getStrongholdAt(child) != null || hasDownstreamStronghold(child)) {
				return true;
			}
		}
		return false;
	}

	// =================================

	private void updateMonarchMovement(double progress) {
		// Case 1: monarch is currently at a node:
		if (state.getMonarchNodeLocation() != null) {
			MapNode newDest = Map.findNeighborInDirection(state.getMonarchNodeLocation(), monarchMovementDirection);
			if (newDest != null) {
				// Monarch is moving in the direction of a new node.
				state.startMovingTowards(newDest);
			}
		}
		// Case 2: monarch is between nodes:
		// NOTE: it is intentional that both cases can be true on the same call when monarch starts moving from node
		if (state.getMonarchEdgeLocation() != null) {
			double movementDistance = getMonarchSpeed() * progress;
			// adjust for difference between night and day lengths:
			if (!state.isDaytime()) {
				movementDistance *= NIGHT_TO_DAY_RATIO;
			}

			MapNode dest = Map.findDestinationOfMovement(state.getMonarchEdgeLocation(), monarchMovementDirection);
			if (dest == null) {
				return;
			}
			if (Map.calculateDistance(new Double(dest.getX(), dest.getY()), state.getMonarchLocation()) < movementDistance) {
				// monarch has arrived at new dest
				state.setMonarchLocation(dest);
				state.exploreNode(dest);
				// stop movement so they don't overshoot:
				monarchMovementDirection = new Double(0, 0);
			}
			else {
				// move along line
				double angle = Map.getAngleOfMovement(state.getMonarchEdgeLocation(), monarchMovementDirection);
				double moveX = Math.cos(Math.toRadians(angle - 90)) * movementDistance;
				double moveY = Math.sin(Math.toRadians(angle - 90)) * movementDistance * -1;
				state.changeMonarchLocation(moveX, moveY);
			}
		}
	}

	// =================================

	public void startMonarchMovementInDirection(Double dir) {
		// Try to add the dir to the current movement direction.
		if (dir.getX() != 0.0) {
			if (dir.getX() == monarchMovementDirection.getX()) {
				// already moving in that direction
				return;
			}
			monarchMovementDirection.setLocation(monarchMovementDirection.getX() + dir.getX(), monarchMovementDirection.getY());
		}
		if (dir.getY() != 0.0) {
			if (dir.getY() == monarchMovementDirection.getY()) {
				// already moving in that direction
				return;
			}
			monarchMovementDirection.setLocation(monarchMovementDirection.getX(), monarchMovementDirection.getY() + dir.getY());
		}
	}

	// =================================

	public void stopMonarchMovementInDirection(Double dir) {
		if (dir.getX() != 0.0) {
			monarchMovementDirection.setLocation(0, monarchMovementDirection.getY());
		}
		if (dir.getY() != 0.0) {
			monarchMovementDirection.setLocation(monarchMovementDirection.getX(), 0);
		}
	}

	// =================================

	public void attemptGoldWithdrawal(int n) {
		// Player is attempting to take some gold from a settlement.
		if (state.getMonarchNodeLocation() == null) {
			System.err.println("WARNING: attempted gold withdrawal while not located at a node");
			return;
		}
		Settlement settlement = state.getSettlementAt(state.getMonarchNodeLocation());
		if (settlement == null) {
			System.err.println("WARNING: attempted gold withdrawal while not at a settlement");
			return;
		}

		// Make sure amount is possible:
		if (n > settlement.getGold()) {
			n = settlement.getGold();
		}
		if (n + state.getMonarchGold() > getMonarchMaxGold()) {
			n = getMonarchMaxGold() - state.getMonarchGold();
		}

		state.addMonarchGold(n);
		settlement.addGold(-1 * n);
	}

	// =================================

	public void attemptGoldDeposit(int n) {
		// Player is attempting to put some gold into a settlement.
		if (state.getMonarchNodeLocation() == null) {
			System.err.println("WARNING: attempted gold withdrawal while not located at a node");
			return;
		}
		Settlement settlement = state.getSettlementAt(state.getMonarchNodeLocation());
		if (settlement == null) {
			System.err.println("WARNING: attempted gold withdrawal while not at a settlement");
			return;
		}
		if (!settlement.allowsGoldDeposits()) {
			System.err.println("WARNING: attempted to deposit gold in an invalid settlement");
			return;
		}

		// Make sure amount if possible:
		if (n > getMonarchGold()) {
			n = getMonarchGold();
		}
		if (n + settlement.getGold() > settlement.getMaxGold()) {
			n = settlement.getMaxGold() - settlement.getGold();
		}

		state.addMonarchGold(-1 * n);
		settlement.addGold(n);
	}

	// =================================

	public int getMonarchMaxGold() {
		return purseSizes[state.getCapital().getLevel() - 1];
	}

	// =================================

	public int getRetinueMax() {
		return retinueLimits[state.getCapital().getLevel() - 1];
	}

	// =================================

	public void attemptRecruitSubject(SubjectType s) {
		MapNode node = state.getMonarchNodeLocation();
		if (node == null || !isNodeCaptured(node)) {
			return;
		}
		if (getPopulationAtNode(node, s) == 0) {
			// No one to recruit.
			return;
		}
		if (state.getRetinueCount() >= getRetinueMax()) {
			return;
		}

		state.addSubjectToRetinue(s);
		state.subtractPopulationAtNode(node, s, this);
	}

	// =================================

	public void attemptLeaveSubject(SubjectType s) {
		// Player is trying to leave a subject at current location
		MapNode node = state.getMonarchNodeLocation();
		if (node == null || !isNodeCaptured(node)) {
			return;
		}
		if (state.getRetinueCount(s) == 0) {
			// There isn't a subject of this type in the retinue
			return;
		}

		state.subtractSubjectFromRetinue(s);
		state.addSubjectToNodePopulation(node, s, this);
	}

	// =================================

	public void attemptEquipSubject(SubjectType subjectType) {
		MapNode node = state.getMonarchNodeLocation();
		if (node == null) {
			System.err.println("WARNING: attempted to equip subject while not located at a node");
			return;
		}
		Settlement settlement = state.getSettlementAt(state.getMonarchNodeLocation());
		if (settlement == null || !settlement.doesSellEquipment(subjectType)) {
			return;
		}
		if (getTotalSubjectsAtNode(node, SubjectType.IDLE) == 0) {
			// No one in retinue to equip
			return;
		}
		int cost = calculateEquipmentCost(subjectType);
		if (getAvailableGold() < cost) {
			// Can't afford it
			return;
		}

		// Success:
		spendMoney(cost);
		// If there is an idle subject in the retinue, convert them:
		if (state.getRetinueCount(SubjectType.IDLE) > 0) {
			state.subtractSubjectFromRetinue(SubjectType.IDLE);
			state.addSubjectToRetinue(subjectType);
		}
		else {
			// Otherwise, convert one at the local settlement:
			state.subtractPopulationAtNode(node, SubjectType.IDLE, this);
			state.addSubjectToNodePopulation(node, subjectType, this);
		}
	}

	// =================================

	public void attemptCaptureNode(MapNode node) {
		if (!canNodeBeCaptured(node)) {
			return;
		}
		// monarch must be at the node:
		if (state.getMonarchNodeLocation() != node) {
			return;
		}
		// Can't be a stronhold there:
		if (state.getStrongholdAt(node) != null) {
			return;
		}
		// must be able to afford it:
		if (state.getMonarchGold() < NODE_CAPTURE_COST) {
			return;
		}

		// Success!
		state.captureNode(node, false);
		state.addMonarchGold(-1 * NODE_CAPTURE_COST);
		Settlement oldSettlement = getSettlementAt(node);
		if (oldSettlement instanceof Peddler) {
			state.removeSettlementAt(node);
		}
		state.getHousingTracker().recalculate(this);

		// move up military units:
		MapNode parentNode = node.getParent();
		int newFrontierCount = 1;
		for (MapNode child: parentNode.getChildren()) {
			if (child != node && !state.isNodeCaptured(child) && hasDownstreamStronghold(child)) {
				newFrontierCount++;
			}
		}
		if (newFrontierCount == 1) {
			// only one child, move all soldiers up:
			while (state.getPopulationAtNode(parentNode, SubjectType.SOLDIER) > 0) {
				state.addSubjectToNodePopulation(node, SubjectType.SOLDIER, this);
				state.subtractPopulationAtNode(parentNode, SubjectType.SOLDIER, this);
			}
			while (state.getPopulationAtNode(parentNode, SubjectType.CAPTAIN) > 0) {
				state.addSubjectToNodePopulation(node, SubjectType.CAPTAIN, this);
				state.subtractPopulationAtNode(parentNode, SubjectType.CAPTAIN, this);
			}
		}
		else {
			// multiple fontiers exist
			int n = state.getPopulationAtNode(parentNode, SubjectType.SOLDIER) / 2;
			for (int i = 0; i < n; i++) {
				state.addSubjectToNodePopulation(node, SubjectType.SOLDIER, this);
				state.subtractPopulationAtNode(parentNode, SubjectType.SOLDIER, this);
			}
		}
	}

	// =================================

	public boolean canNodeBeCaptured(MapNode node) {
		// note that there are additional checks in attemptCaptureNode

		// node must not be captured:
		if (isNodeCaptured(node)) {
			return false;
		}
		// Can't be a stronghold at it
		if (state.getStrongholdAt(node) != null) {
			return false;
		}
		// it must border a friendly node that was not captured this turn:
		boolean foundOne = false;
		for (MapNode neighbor: node.getNeighbors()) {
			if (isNodeCaptured(neighbor) && !state.hasNodeBeenCapturedThisTurn(neighbor)) {
				foundOne = true;
				break;
			}
		}
		if (!foundOne) {
			return false;
		}

		// can only capture during the day:
		if (!isDaytime()) {
			return false;
		}

		return true;
	}

	// =================================

	public void newSubjectAtSettlement(Settlement settlement) {
		// this city has produced a new citizen
		MapNode node = state.findLocationOfSettlement(settlement);
		state.addSubjectToNodePopulation(node, SubjectType.IDLE, this);
	}

	// =================================

	public void attemptBuildNewCity(MapNode node) {
		// node must be captured and there cannot already be a settlement there:
		if (!isNodeCaptured(node) || getSettlementAt(node) != null) {
			return;
		}
		// player must be able to afford it:
		if (state.getMonarchGold() < City.getCost(1)) {
			return;
		}
		if(isTooCloseToCity(node)) {
			return;
		}

		// success:
		state.addMonarchGold(-1 * City.getCost(1));
		state.addSettlement(new City(), node);
		state.getHousingTracker().recalculate(this); // b/c there is one less open node
	}

	// =================================

	public void attemptBuildNewFarm(MapNode node) {
		// node must be captured and there cannot already be a settlement there:
		if (!isNodeCaptured(node) || getSettlementAt(node) != null) {
			return;
		}
		// player must be able to afford it:
		if (state.getMonarchGold() < Farm.getCost(1)) {
			return;
		}
		// Tech must be unlocked by palace level:
		if (state.getCapital().getLevel() < FARM_UNLOCK_LEVEL) {
			return;
		}
		if (!isNodeArable(node)) {
			return;
		}

		// success:
		state.addMonarchGold(-1 * Farm.getCost(1));
		state.addSettlement(new Farm(), node);
		state.getHousingTracker().recalculate(this); // b/c there is one less open node
	}

	// =================================

	public void attemptBuildGoldMine() {
		MapNode node = state.getMonarchNodeLocation();
		if (node == null || !isNodeCaptured(node) || state.getSettlementAt(node) != null) {
			return;
		}
		if (!isNodeGoldVein(node)) {
			return;
		}
		if (state.getMonarchGold() < GoldMine.getCost()) {
			return;
		}

		// success:
		state.addMonarchGold(-1 * GoldMine.getCost());
		state.addSettlement(new GoldMine(), node);
		state.getHousingTracker().recalculate(this); // b/c there is one less open node
	}

	// =================================

	public int getTotalSubjectsAtNode(MapNode node, SubjectType subjectType) {
		// how many of a certain kind of subject are at a settlement, counting its pop and the retinue?
		int n = state.getPopulationAtNode(node, subjectType);
		if (state.getMonarchNodeLocation() == node) {
			n += state.getRetinueCount(subjectType);
		}
		return n;
	}

	public int getTotalSubjectsAtSettlement(Settlement s, SubjectType subjectType) {
		return getTotalSubjectsAtNode(findLocationOfSettlement(s), subjectType);
	}

	public int getTotalSubjectsAtWall(Wall w, SubjectType subjectType) {
		return getTotalSubjectsAtNode(state.findLocationOfWall(w), subjectType);
	}

	// =================================

	public boolean isSettlementUpgradeUnlocked(Settlement settlement) {
		// Palace upgrades are required to access some high level settlements
		int palaceLevel = state.getCapital().getLevel();
		if (settlement instanceof City) {
			// Cities can reach but not exceed capital city level
			return settlement.getLevel() < palaceLevel;
		}
		if (settlement instanceof Farm) {
			if (settlement.getLevel() == 1) {
				return palaceLevel >= 3;
			}
			else {
				return palaceLevel >= 5;
			}
		}
		if (settlement instanceof Capital) {
			return true;
		}

		// default:
		return false;
	}

	// =================================

	public void attemptUpgradeSettlement() {
		if (getMonarchNodeLocation() == null) {
			return;
		}
		Settlement settlement = state.getSettlementAt(state.getMonarchNodeLocation());
		// settlement must exist and not be max level:
		if (settlement == null || settlement.isUpgrading() || settlement.getLevel() >= settlement.getMaxLevel()) {
			return;
		}
		if (!isSettlementUpgradeUnlocked(settlement)) {
			return;
		}
		if (getAvailableGold() < settlement.getUpgradeCost()) {
			return;
		}

		// success:
		spendMoney(settlement.getUpgradeCost());
		settlement.beginUpgrade();
	}

	// =================================

	public int getDefenseRating(MapNode node) {
		int rating = 0;

		int archerStrength = DEFENSE_PER_SOLDIER * getTotalSubjectsAtNode(node, SubjectType.SOLDIER);
		if (getTotalSubjectsAtNode(node, SubjectType.CAPTAIN) > 0) {
			archerStrength = (int)Math.round(archerStrength * CAPTAIN_STRENGTH_BONUS);
		}
		rating += archerStrength;

		Wall wall = state.getWallAtNode(node);
		if (wall != null && !wall.isBuildingSite()) {
			rating += wall.getDefenseRating();
		}
		else {
			rating += 1; // free defense point for wall-less nodes
		}

		return rating;
	}

	// =================================

	public void attemptBuildOrUpgradeWall() {
		MapNode node = state.getMonarchNodeLocation();
		if (node == null || !state.isNodeCaptured(node)) {
			return;
		}
		Settlement settlement = state.getSettlementAt(node);
		if (settlement != null && settlement.isBuildingSite()) {
			// Only one kind of construction at a time:
			return;
		}
		Wall wall = getWallAtNode(node);
		
		// building a new wall:
		if (wall == null) {
			if (getAvailableGold() >= Wall.getCost(1)) {
				// success:
				spendMoney(Wall.getCost(1));
				state.addWall(new Wall(), node);
			}
		}
		// upgrading a wall:
		else {
			if (wall.isUpgrading() || wall.getLevel() >= Wall.MAX_LEVEL) {
				return;
			}
			if (getAvailableGold() >= wall.getUpgradeCost()) {
				// success:
				spendMoney(wall.getUpgradeCost());
				wall.beginUpgrade();
			}
		}
	}

	// =================================

	private void spendMoney(int n) {
		// spends from local treasury, then from purse
		MapNode node = state.getMonarchNodeLocation();
		Settlement settlement = node == null ? null : state.getSettlementAt(node);
		if (settlement == null) {
			state.addMonarchGold(-1 * n);
		}
		else {
			if (settlement.getGold() >= n) {
				settlement.addGold(-1 * n);
			}
			else {
				n -= settlement.getGold();
				settlement.addGold(-1 * settlement.getGold());
				state.addMonarchGold(-1 * n);
			}
		}
	}

	// =================================

	private int getAvailableGold() {
		// Finds how much monarch has to spend between the local treasury and their purse
		MapNode node = state.getMonarchNodeLocation();
		Settlement settlement = node == null ? null : state.getSettlementAt(node);
		if (settlement == null) {
			return state.getMonarchGold();
		}
		else {
			return state.getMonarchGold() + settlement.getGold();
		}
	}

	// =================================

	public boolean canSortieBeLaunched(MapNode node) {
		// Can it be launched from this node?
		// must be a frontier:
		if (!isFrontier(node)) {
			return false;
		}
		// must not already have a sortie:
		if (state.getSortieAt(node) != null) {
			return false;
		}
		// must not be a leaf:
		if (node.getChildren().size() == 0) {
			return false;
		}
		// must have an explored stronghold among its children:
		if (findClosestExploredStronghold(node) == null) {
			return false;
		}
		// must have at least one soldier there:
		if (getTotalSubjectsAtNode(node, SubjectType.SOLDIER) == 0) {
			return false;
		}
		// must be daytime:
		if (!state.isDaytime()) {
			return false;
		}

		return true;
	}

	// =================================

	public boolean isFrontier(MapNode node) {
		// Must be captured AND border a non-captured node
		if (!state.isNodeCaptured(node)) {
			return false;
		}
		for (MapNode child: node.getChildren()) {
			if (!state.isNodeCaptured(child)) {
				return true;
			}
		}
		return false;
	}

	// =================================

	public Stronghold findClosestExploredStronghold(MapNode node) {
		Stronghold closest = null;
		int closestDistance = 0;
		LinkedList<MapNode> searchQueue = new LinkedList<MapNode>();
		searchQueue.addAll(node.getChildren());
		while (searchQueue.size() > 0) {
			MapNode search = searchQueue.removeFirst();
			if (!state.isNodeExplored(search)) {
				continue;
			}
			Stronghold stronghold = state.getStrongholdAt(search);
			if (stronghold != null) {
				int d = Map.getDistance(search, node);
				if (closest == null || d < closestDistance) {
					closest = stronghold;
					closestDistance = d;
					continue; // no need to add children b/c they can't contain a closer one
				}
			}
			searchQueue.addAll(search.getChildren());
		}
		return closest;
	}

	// =================================

	public void attemptLaunchSortie() {
		MapNode node = state.getMonarchNodeLocation();
		if (node == null || !canSortieBeLaunched(node)) {
			return;
		}

		state.addSortie(new Sortie(node, findClosestExploredStronghold(node), this));
	}

	// =================================

	public void sortieVictory(Sortie s) {
		state.removeSortie(s);
		state.removeStronghold(s.getTarget());
		if (state.countStrongholds() == 0) {
			state.setGameResult(true);
			throw new GameOverException();
		}
		if (state.getMonarchNodeLocation() == s.getLocation() 
			&& !hasDownstreamStronghold(s.getLocation())
			&& state.getSpecialRetinue() == null) 
		{
			formVictoriousArmy();
		}
	}

	// =================================

	private void formVictoriousArmy() {
		// pull all military units at the monarch's location into a special retinue
		Population specialRet = new Population();
		MapNode loc = state.getMonarchNodeLocation();
		for (SubjectType st: SubjectType.values()) {
			if (!st.isCivilian()) {
				while (state.getPopulationAtNode(loc, st) > 0) {
					state.subtractPopulationAtNode(loc, st, this);
					specialRet.addSubject(st);
				}
			}
		}
		state.setSpecialRetinue("Victorious army", specialRet);
	}

	// =================================

	public void inflictSortieCasaulty(Sortie sortie) {
		MapNode node = sortie.getLocation();
		// case 1: there is a soldier at the node:
		if (getPopulationAtNode(node, SubjectType.SOLDIER) > 0) {
			state.subtractPopulationAtNode(node, SubjectType.SOLDIER, this);
		}
		// case 2: there is a soldier in the retinue:
		else if (state.getRetinueCount(SubjectType.SOLDIER) > 0) {
			state.subtractSubjectFromRetinue(SubjectType.SOLDIER);
		}
		// case 3: captain at the node:
		else if (getPopulationAtNode(node, SubjectType.CAPTAIN) > 0) {
			state.subtractPopulationAtNode(node, SubjectType.CAPTAIN, this);
		}
		// case 4: captain in retinue:
		else if (state.getRetinueCount(SubjectType.CAPTAIN) > 0) {
			state.subtractSubjectFromRetinue(SubjectType.CAPTAIN);
		}

		// check to see if sortie is out of military units:
		if (getTotalSubjectsAtNode(node, SubjectType.SOLDIER) == 0 && getTotalSubjectsAtNode(node, SubjectType.CAPTAIN) == 0) {
			state.removeSortie(sortie);
		}
	}

	// =================================

	public void cancelSortie(Sortie sortie) {
		state.removeSortie(sortie);
	}

	// =================================

	private void gameOver(boolean playerWon) {
		state.setGameResult(playerWon);
		throw new GameOverException();
	}

	// =================================

	public void depositExcessFromFarm(Settlement settlement, int amount) {
		// *or gold mine
		// excess profits from farms are deposited in adjacent cities (with room in treasuries)
		MapNode farmNode = state.findLocationOfSettlement(settlement);
		for (MapNode node: farmNode.getNeighbors()) {
			Settlement s = state.getSettlementAt(node);
			if (s != null) {
				if (s instanceof City) {
					int room = ((City)s).getMaxGold() - ((City)s).getGold();
					((City)s).addGold(Math.min(amount, room));
					amount -= room;
					if (amount <= 0) {
						return;
					}
				}
				else if (s instanceof Capital) {
					int room = ((Capital)s).getMaxGold() - ((Capital)s).getGold();
					((Capital)s).addGold(Math.min(amount, room));
					amount -= room;
					if (amount <= 0) {
						return;
					}
				}
			}
		}
	}

	// =================================

	private double getMonarchSpeed() {
		return MONARCH_BASE_MOVE_SPEED * (1.0 + getMonarchMoveBonus());
	}

	// =================================

	public int calculateEquipmentCost(SubjectType subjectType) {
		int cost = subjectType.getBaseEquipmentCost();
		// workers and soldiers get more expensive the more the player controls:
		if (subjectType == SubjectType.SOLDIER) {
			cost += (int)(MARGINAL_EQUIP_COST_SOLDIER * state.getTotalKingdomPopulation(subjectType));
			if (cost > MAX_SOLDIER_EQUIP_COST) {
				cost = MAX_SOLDIER_EQUIP_COST;
			}
		}
		else if (subjectType == SubjectType.WORKER) {
			cost += (int)(MARGINAL_EQUIP_COST_WORKER * state.getTotalKingdomPopulation(subjectType));
		}
		return cost;
	}

	// =================================

	public void attemptLeaveSpecialRetinue() {
		MapNode node = state.getMonarchNodeLocation();
		if (node == null || state.getSpecialRetinue() == null) {
			return;
		}
		if (!state.isNodeCaptured(node)) {
			return;
		}

		Population specialRet = state.getSpecialRetinue();
		for (SubjectType st: SubjectType.values()) {
			for (int i = 0; i < specialRet.getCount(st); i++) {
				state.addSubjectToNodePopulation(node, st, this);
			}
		}
		state.clearSpecialRetinue();
	}

	// =================================
	/*
	public int getTotalHousingCapacity() {
		double capacity = 0;
		// add a little for open nodes:
		for (MapNode node: state.getMap().getNodeList()) {
			if (state.isNodeCaptured(node) && state.getSettlementAt(node) == null) {
				capacity += HOUSING_PER_OPEN_NODE;
			}
		}
		// add housing from settlements:
		for (Settlement s: state.getAllSettlements()) {
			if (!s.isBuildingSite()) {
				capacity += s.getHousingCapacity();
			}
		}

		return (int)Math.round(capacity);
	}*/

	// =================================

	public PopGrowthModifier getPopGrowthModifier() {
		return PopGrowthModifier.getCurrentModifier((double)state.getTotalKingdomPopulation().getTotal() / state.getHousingTracker().getTotal());
	}

	// =================================

	public boolean isTooCloseToCity(MapNode node) {
		// essentially asks if a new city can be built at the node w/o being too close to another one
		LinkedList<MapNode> search = new LinkedList<MapNode>();
		search.add(node);
		for (int  i = 0; i < CITY_EXCLUSION_DISTANCE; i++) {
			LinkedList<MapNode> searchCopy = new LinkedList<MapNode>();
			searchCopy.addAll(search);
			for (MapNode n: searchCopy) {
				for (MapNode neighbor: n.getNeighbors()) {
					if (!search.contains(neighbor)) {
						search.add(neighbor);
					}
				}
			}
		}
		for (MapNode n: search) {
			Settlement s = state.getSettlementAt(n);
			if (s != null && (s instanceof City || s instanceof Capital)) {
				return true;
			}
		}
		return false;
	}

	// =================================

	public static int determineGoldVeinAmount(int depth) {
		// decides how much gold is in a particualr vein
		// arg is depth of vein's location from root
		return 60 + (int)(Math.random() * 60) + 3 * depth;
	}

	// =================================

	public void decrementGoldInVein(GoldMine g) {
		state.decrementGoldInVein(state.findLocationOfSettlement(g));
	}

	// =================================

	public void onSettlementUpgradeFinished(Settlement settlement) {
		if (settlement instanceof City || settlement instanceof Capital) {
			state.getHousingTracker().recalculate(this);
		}
	}

	// =================================

	public void onBuildingSiteFinished(Settlement settlement) {
		if (settlement instanceof City) {
			state.getHousingTracker().recalculate(this);
		}
	}

	// =========== ACCESSORS ==============

	public MapNode findLocationOfSettlement(Settlement s) {
		return state.findLocationOfSettlement(s);
	}

	public MapNode findLocationOfStronghold(Stronghold s) {
		return state.findLocationOfStronghold(s);
	}

	public Collection<Settlement> getAllSettlements() {
		return state.getAllSettlements();
	}

	public FrontierAttack getAttackAtNode(MapNode node) {
		for (FrontierAttack attack: state.getFrontierAttacks()) {
			if (attack.getTargetNode() == node) {
				return attack;
			}
		}
		return null;
	}

	public Capital getCapital() {
		return state.getCapital();
	}

	public double getDangerLevel() {
		return state.getDangerLevel();
	}

	public int getDayCount() {
		return state.getDayCount();
	}

	public double getDayNightTime() {
		return state.getDayNightTime();
	}

	public boolean getGameResult() {
		return state.getGameResult();
	}

	public int getGoldVeinAmount(MapNode node) {
		return state.getGoldVeinAmount(node);
	}

	public int getGoldVeinAmount(GoldMine g) {
		return state.getGoldVeinAmount(state.findLocationOfSettlement(g));
	}

	public Map getMap() {
		return state.getMap();
	}

	public int getMonarchGold() {
		return state.getMonarchGold();
	}

	public Double getMonarchLocation() {
		return state.getMonarchLocation();
	}

	public double getMonarchMoveBonus() {
		return monarchMoveSpeedBonuses[state.getCapital().getLevel() - 1];
	}

	public MapNode getMonarchNodeLocation() {
		return state.getMonarchNodeLocation();
	}

	public int getPopCountAtNode(MapNode node) {
		return state.getPopCountAtNode(node);
	}

	public int getPopulationAtNode(MapNode node, SubjectType s) {
		return state.getPopulationAtNode(node, s);
	}

	public int getRetinueCount() {
		return state.getRetinueCount();
	}

	public int getRetinueCount(SubjectType s) {
		return state.getRetinueCount(s);
	}

	public Settlement getSettlementAt(MapNode node) {
		return state.getSettlementAt(node);
	}

	public Population getSpecialRetinue() {
		return state.getSpecialRetinue();
	}

	public String getSpecialRetinueName() {
		return state.getSpecialRetinueName();
	}

	public Sortie getSortieAt(MapNode node) {
		return state.getSortieAt(node);
	}

	public Sortie getSortieTargeting(Stronghold stronghold) {
		return state.getSortieTargeting(stronghold);
	}

	public Stronghold getStrongholdAt(MapNode node) {
		return state.getStrongholdAt(node);
	}

	public HousingTracker getHousingTracker() {
		return state.getHousingTracker();
	}

	public Population getTotalKingdomPopulation() {
		return state.getTotalKingdomPopulation();
	}

	public Wall getWallAtNode(MapNode node) {
		return state.getWallAtNode(node);
	}

	public boolean isDaytime() {
		return state.isDaytime();
	}

	public boolean isNodeArable(MapNode node) {
		return state.getArableLand().contains(node);
	}

	public boolean isNodeCaptured(MapNode node) {
		return state.isNodeCaptured(node);
	}

	public boolean isNodeExplored(MapNode node) {
		return state.isNodeExplored(node);
	}

	public boolean isNodeGoldVein(MapNode node) {
		return state.getMap().getGoldVeins().contains(node);
	}


}