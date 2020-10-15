// Stores all information needed about a game instance. Should contain no game logic.

package core;
import java.awt.geom.Point2D.Double;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;


public class GameState implements Serializable {

	private Map map;
	private DifficultyLevel difficulty;
	private int dayCounter;
	private Double monarchLocation; // exact map coords, used only when monarch is between nodes
	private MapNode monarchLocationNode; // used when monarch is at a node
	private MapEdge monarchLocationEdge;
	private HashMap<MapNode, Boolean> exploredNodes;
	private HashMap<MapNode, Boolean> humanNodes; // ones captured by the player
	private boolean isDaytime;
	private double dayNightTime; // always between 0 and 1.0, records progress through either the day or the night
	private int monarchGold; // how much they are carrying
	private HashMap<MapNode, Settlement> settlements;
	private HashMap<MapNode, Population> populations; // tracks all subjects at all nodes
	private Population retinue;
	private LinkedList<MapNode> capturedThisTurn; // all nodes captured on this day
	private HashMap<MapNode, Wall> walls;
	private double dangerLevel;
	private LinkedList<FrontierAttack> attacks;
	private HashMap<MapNode, Stronghold> strongholds;
	private LinkedList<Sortie> sorties;
	private boolean isGameOver;
	private boolean gameResult; // did the player win?
	private Population totalKingdomPopulation;
	private Population specialRetinue; // a group of subjects following the monarch but different from normal retinue
	private String specialRetinueName;
	private HashMap<MapNode, Integer> goldVeins; // int portion is the amount of gold in them
	private HousingTracker housingTracker;


	public GameState(GameInstance instance, Map m, DifficultyLevel d) {
		map = m;
		difficulty = d;
		exploredNodes = new HashMap<MapNode, Boolean>();
		humanNodes = new HashMap<MapNode, Boolean>();
		settlements = new HashMap<MapNode, Settlement>();
		isDaytime = true;
		dayCounter = 1;
		populations = new HashMap<MapNode, Population>();
		for (MapNode node: map.getNodeList()) {
			populations.put(node, new Population());
		}
		retinue = new Population();
		totalKingdomPopulation = new Population();
		capturedThisTurn = new LinkedList<MapNode>();
		walls = new HashMap<MapNode, Wall>();
		attacks = new LinkedList<FrontierAttack>();
		strongholds = new HashMap<MapNode, Stronghold>();
		sorties = new LinkedList<Sortie>();
		goldVeins = new HashMap<MapNode, Integer>();
		for (MapNode n: map.getGoldVeins()) {
			goldVeins.put(n, GameInstance.determineGoldVeinAmount(map.getDistance(n, map.getRootNode())));
		}
		housingTracker = new HousingTracker();
	}

	// ===================================================

	public boolean updateDayNightTime(double progress) {
		// Returns true is either a day or night has changed
		dayNightTime += progress;
		if (dayNightTime >= 1.0) {
			dayNightTime = 0.0;
			isDaytime = !isDaytime;

			if (isDaytime) {
				dayCounter++;
				capturedThisTurn.clear();
			}
			return true;
		}
		else {
			return false;
		}
	}

	// ===================================================

	public void startMovingTowards(MapNode dest) {
		// Update all monarch location fields to capture the fact that they have moved off a node and towards a new one.
		monarchLocation = new Double(monarchLocationNode.getX(), monarchLocationNode.getY());
		monarchLocationEdge = new MapEdge(monarchLocationNode, dest);
		monarchLocationNode = null;
	}

	// ===================================================

	public void captureNode(MapNode node, boolean ignoreCaptureDelay) {
		// the 'capture delay' is what prevents a frontier from being advanced twice in a day
		// it is ignored when capturing nodes at the beginning of the game
		humanNodes.put(node, true);
		if (!ignoreCaptureDelay) {
			capturedThisTurn.add(node);
		}

		totalKingdomPopulation.addAll(populations.get(node));
	}

	public void uncaptureNode(MapNode node) {
		humanNodes.remove(node);
	}

	// ===================================================

	public void clearFrontierAttacks() {
		attacks.clear();
	}

	// ===================================================

	public LinkedList<MapNode> getFrontiers() {
		// Finds all nodes which are captured but border non-captured nodes
		LinkedList<MapNode> list = new LinkedList<MapNode>();
		for (MapNode node: map.getNodeList()) {
			if (!isNodeCaptured(node)) {
				continue;
			}
			// Look for a non-captured child:
			for (MapNode child: node.getChildren()) {
				if (!isNodeCaptured(child)) {
					list.add(node);
					break;
				}
			}
		}
		return list;
	}

	// ===================================================

	public void clearPopulationAtNode(MapNode node) {
		populations.put(node, new Population());
	}

	// ===================================================

	public void clearSorties() {
		sorties.clear();
	}

	// ===================================================

	public void setSpecialRetinue(String name, Population ret) {
		specialRetinueName = name;
		specialRetinue = ret;
		totalKingdomPopulation.addAll(ret);
	}

	// ===================================================

	public void clearSpecialRetinue() {
		totalKingdomPopulation.subtractAll(specialRetinue);
		specialRetinue = null;
		specialRetinueName = null;
	}

	// ================== ACCESSORS =======================

	public void addFrontierAttack(FrontierAttack a) {
		attacks.add(a);
	}

	public void addMonarchGold(int n) {
		monarchGold += n;
	}

	public void addSettlement(Settlement s, MapNode node) {
		settlements.put(node, s);
	}

	public void addSortie(Sortie s) {
		sorties.add(s);
	}

	public void addStronghold(MapNode node, Stronghold s) {
		strongholds.put(node, s);
	}

	public void addSubjectToNodePopulation(MapNode node, SubjectType s, GameInstance instance) {
		populations.get(node).addSubject(s);
		if (isNodeCaptured(node)) {
			totalKingdomPopulation.addSubject(s);
		}
		if (getSettlementAt(node) != null) {
			getSettlementAt(node).onSubjectAdded(s, instance);
		}
	}

	public void addSubjectToRetinue(SubjectType s) {
		retinue.addSubject(s);
		totalKingdomPopulation.addSubject(s);
	}

	public void addWall(Wall w, MapNode node) {
		walls.put(node, w);
	}

	public void changeMonarchLocation(double dx, double dy) {
		monarchLocation.setLocation(monarchLocation.getX() + dx, monarchLocation.getY() + dy);
	}

	public int countStrongholds() {
		int count = 0;
		for (MapNode node: strongholds.keySet()) {
			if (strongholds.get(node) != null) {
				count++;
			}
		}
		return count;
	}

	public void decrementGoldInVein(MapNode node) {
		goldVeins.put(node, goldVeins.get(node).intValue() - 1);
	}

	public void exploreNode(MapNode n) {
		exploredNodes.put(n, true);
	}

	public MapNode findLocationOfSettlement(Settlement s) {
		for (MapNode node: settlements.keySet()) {
			if (settlements.get(node) == s) {
				return node;
			}
		}
		return null;
	}

	public MapNode findLocationOfStronghold(Stronghold stronghold) {
		for (MapNode node: strongholds.keySet()) {
			if (strongholds.get(node) == stronghold) {
				return node;
			}
		}
		return null;
	}

	public MapNode findLocationOfWall(Wall w) {
		for (MapNode node: walls.keySet()) {
			if (walls.get(node) == w) {
				return node;
			}
		}
		return null;
	}

	public Collection<Settlement> getAllSettlements() {
		return settlements.values();
	}

	public Collection<Wall> getAllWalls() {
		return walls.values();
	}

	public LinkedList<MapNode> getArableLand() {
		return map.getArableLand();
	}

	public Capital getCapital() {
		return (Capital)(getSettlementAt(map.getRootNode()));
	}

	public double getDangerLevel() {
		return dangerLevel;
	}

	public int getDayCount() {
		return dayCounter;
	}

	public double getDayNightTime() {
		return dayNightTime;
	}

	public DifficultyLevel getDifficultyLevel() {
		return difficulty;
	}

	public LinkedList<FrontierAttack> getFrontierAttacks() {
		return attacks;
	}

	public boolean getGameResult() {
		return gameResult;
	}

	public int getGoldVeinAmount(MapNode node) {
		if (goldVeins.get(node) == null) {
			return 0;
		}
		return goldVeins.get(node).intValue();
	}

	public HousingTracker getHousingTracker() {
		return housingTracker;
	}

	public Map getMap() {
		return map;
	}

	public MapEdge getMonarchEdgeLocation() {
		return monarchLocationEdge;
	}

	public int getMonarchGold() {
		return monarchGold;
	}

	public int getPopCountAtNode(MapNode node) {
		return populations.get(node).getTotal();
	}

	public int getPopulationAtNode(MapNode node, SubjectType s) {
		return populations.get(node).getCount(s);
	}

	public Double getMonarchLocation() {
		// always returns a point, even when monarch is at a node
		if (monarchLocationNode != null) {
			return new Double(monarchLocationNode.getX(), monarchLocationNode.getY());
		}
		return monarchLocation;
	}

	public MapNode getMonarchNodeLocation() {
		// always return the node, even if monarch is not at one
		return monarchLocationNode;
	}

	public int getRetinueCount() {
		return retinue.getTotal();
	}

	public int getRetinueCount(SubjectType s) {
		return retinue.getCount(s);
	}

	public Settlement getSettlementAt(MapNode node) {
		return settlements.get(node);
	}

	public Population getSpecialRetinue() {
		return specialRetinue;
	}

	public String getSpecialRetinueName() {
		return specialRetinueName;
	}

	public Sortie getSortieAt(MapNode node) {
		for (Sortie s: sorties) {
			if (s.getLocation() == node) {
				return s;
			}
		}
		return null;
	}

	public LinkedList<Sortie> getSorties() {
		return sorties;
	}

	public Sortie getSortieTargeting(Stronghold stronghold) {
		for (Sortie s: sorties) {
			if (s.getTarget() == stronghold) {
				return s;
			}
		}
		return null;
	}

	public Stronghold getStrongholdAt(MapNode node) {
		return strongholds.get(node);
	}

	public Population getTotalKingdomPopulation() {
		return totalKingdomPopulation;
	}

	public int getTotalKingdomPopulation(SubjectType st) {
		return totalKingdomPopulation.getCount(st);
	}

	public Wall getWallAtNode(MapNode node) {
		return walls.get(node);
	}

	public boolean hasNodeBeenCapturedThisTurn(MapNode node) {
		return capturedThisTurn.contains(node);
	}

	public void increaseDangerLevel(double d) {
		dangerLevel += d;
	}

	public boolean isDaytime() {
		return isDaytime;
	}

	public boolean isNodeCaptured(MapNode node) {
		return humanNodes.get(node) != null && humanNodes.get(node).booleanValue();
	}

	public boolean isNodeExplored(MapNode node) {
		return exploredNodes.get(node) != null && exploredNodes.get(node).booleanValue();
	}

	public void removeSettlementAt(MapNode node) {
		settlements.remove(node);
	}

	public void removeSortie(Sortie s) {
		sorties.remove(s);
	}

	public void removeStronghold(Stronghold s) {
		strongholds.remove(findLocationOfStronghold(s));
	}

	public void removeWallAt(MapNode node) {
		walls.remove(node);
	}

	public void setGameResult(boolean playerWon) {
		isGameOver = true;
		gameResult = playerWon;
	}

	public void setMonarchLocation(MapNode node) {
		monarchLocationNode = node;
		monarchLocation = null;
		monarchLocationEdge = null;
	}

	public void subtractPopulationAtNode(MapNode node, SubjectType s, GameInstance instance) {
		populations.get(node).subtractSubject(s);
		if (getSettlementAt(node) != null) {
			getSettlementAt(node).onSubjectSubtracted(s, instance);
		}
		if (isNodeCaptured(node)) {
			totalKingdomPopulation.subtractSubject(s);
		}
	}

	public void subtractSubjectFromRetinue(SubjectType s) {
		retinue.subtractSubject(s);
		totalKingdomPopulation.subtractSubject(s);
	}


}