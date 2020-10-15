// Instance of a mine settlement at a gold vein

package core;


public class GoldMine extends Settlement {

	private static final int COST = 20;
	private static final int TREASURY_SIZE = 16;
	private static final double CONSTRUCTION_TIME = 0.75;
	private static final double GOLD_PROD_RATE = 5.0; // gold per worker per day
	private static double nextGoldProgress;


	public GoldMine() {
		isBuildingSite = true;
		level = 1;
	}

	// ============================================

	protected void update(GameInstance instance, double progress) {
		if (isBuildingSite) {
			double newProgress = progress * instance.getTotalSubjectsAtSettlement(this, SubjectType.WORKER) / CONSTRUCTION_TIME;
			constructionProgress += newProgress;
			if (constructionProgress >= 1.0) {
				constructionProgress = 0.0;
				isBuildingSite = false;
				instance.onBuildingSiteFinished(this);
			}
		}
		else {
			// produce gold:
			Wall wall = instance.getWallAtNode(instance.findLocationOfSettlement(this));
			if (isTreasurySpaceAvailable(instance) 
				&& instance.getGoldVeinAmount(this) > 0
				&& (wall == null || (!wall.isBuildingSite() && !wall.isUpgrading()))) 
			{
				nextGoldProgress += progress * GOLD_PROD_RATE * instance.getTotalSubjectsAtSettlement(this, SubjectType.WORKER);
				if (nextGoldProgress > 1.0) {
					nextGoldProgress = 0.0;
					if (gold >= TREASURY_SIZE) {
						instance.depositExcessFromFarm(this, 1);
					}
					else {
						gold++;
					}
					instance.decrementGoldInVein(this);
				}
			}
		}
	}

	// ============================================

	private boolean isTreasurySpaceAvailable(GameInstance instance) {
		// is there room for one gold to be deposited in this treasury or a neighboring city?
		if (gold < TREASURY_SIZE) {
			return true;
		}
		for (MapNode neighbor: instance.findLocationOfSettlement(this).getNeighbors()) {
			Settlement s = instance.getSettlementAt(neighbor);
			if (s != null && s instanceof City) {
				City city = (City)s;
				if (city.getGold() < city.getMaxGold()) {
					return true;
				}
			}
			if (s != null && s instanceof Capital) {
				Capital cap = (Capital)s;
				if (cap.getGold() < cap.getMaxGold()) {
					return true;
				}
			}
		}
		return false;
	}

	// ============================================

	public void onSubjectAdded(SubjectType s, GameInstance instance) {
		// do nothing
	}


	public void onSubjectSubtracted(SubjectType s, GameInstance instance) {
		// do nothing
	}

	protected void onUpgradeFinished(GameInstance instance) {
		// do nothing
	}

	// =============== ACCESSORS ==================

	public boolean allowsGoldDeposits() {
		return false;
	}

	public boolean doesSellEquipment(SubjectType s) {
		return false;
	}

	public static int getCost() {
		return COST;
	}

	public int getHousingCapacity() {
		return 0;
	}

	public int getMaxGold() {
		return TREASURY_SIZE;
	}

	public int getMaxLevel() {
		return 1;
	}

	public double getNextGoldProgress() {
		return nextGoldProgress;
	}

	public int getUpgradeCost() {
		// only has one level, so there is no such thing
		return 0;
	}

	public double getUpgradeConstructionTime() {
		return 0.0;
	}

	public String toString() {
		return "Gold mine";
	}


}