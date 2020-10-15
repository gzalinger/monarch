// A ind of settlement that sells equips and generates new citizens

package core;
import java.io.Serializable;


public class City extends Settlement implements Serializable {

	private static double[] newPopPerDay = new double[]{0.26, 0.51, 1.01, 1.51, 2.51}; // indices are levels
	private static int[] buildingAndUpgradingCosts = new int[]{16, 12, 22, 30, 60};
	private static double[] constructionTimes = new double[]{1.0, 2.0, 2.5, 3.5, 5.0}; // unit is worker-days
	private static int[] treasurySizes = new int[]{20, 50, 75, 120, 180};
	private static int[] housingCapacities = new int[]{4, 6, 10, 14, 20};
	private double nextSubjectProgress; // how close it is to producing another subject


	public City() {
		isBuildingSite = true;
		level = 1;
	}

	// ====================================

	protected void update(GameInstance instance, double progress) {
		if (isBuildingSite) {
			double newProgress = progress * instance.getTotalSubjectsAtSettlement(this, SubjectType.WORKER) / constructionTimes[0];
			constructionProgress += newProgress;
			if (constructionProgress >= 1.0) {
				constructionProgress = 0.0;
				isBuildingSite = false;
				instance.onBuildingSiteFinished(this);
			}
		}

		if (!isBuildingSite) {
			// NOTE that this was copied from Capital
			nextSubjectProgress += progress * City.getNewPopPerDay(level) * instance.getPopGrowthModifier().getGrowthMod();
			if (nextSubjectProgress >= 1.0) {
				nextSubjectProgress = 0.0;
				instance.newSubjectAtSettlement(this);
			}
		}
	}
	
	// ====================================

	public boolean doesSellEquipment(SubjectType s) {
		switch (s) {
			case WORKER:
			case SOLDIER:
				return true;

			case FARMER:
				return level >= 2;

			default: return false;
		}
	}

	// ====================================

	public void onSubjectAdded(SubjectType s, GameInstance instances) {
		// do nothing
	}

	// ====================================

	public void onSubjectSubtracted(SubjectType s, GameInstance instance){
		// do nothing
	}

	// ====================================

	protected void onUpgradeFinished(GameInstance instance) {
		// do nothing
	}


	// ======= STATIC ACCESSORS ===========

	public static int getCost(int level) {
		// returns the cost of upgrading to the given level
		return buildingAndUpgradingCosts[level - 1];
	}

	public static double getNewPopPerDay(int level) {
		return newPopPerDay[level - 1];
	}

	// ============== ACCESSORS ===============

	public boolean allowsGoldDeposits() {
		return true;
	}

	public int getHousingCapacity() {
		return housingCapacities[level - 1];
	}

	public int getMaxGold() {
		return treasurySizes[level - 1];
	}

	public int getMaxLevel() {
		// the max levels cities can reach in this game
		return 5;
	}

	public double getNewPopProgress() {
		return nextSubjectProgress;
	}

	public double getUpgradeConstructionTime() {
		// time in worker-days to build next level
		if (level == getMaxLevel()) {
			return 0.0;
		}
		return constructionTimes[level];
	}

	public int getUpgradeCost() {
		if (level == getMaxLevel()) {
			return 0;
		}
		return buildingAndUpgradingCosts[level];
	}

	public String toString() {
		return "City";
	}

}