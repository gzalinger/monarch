// The capital city / palace settlement

package core;
import java.io.Serializable;


public class Capital extends Settlement implements Serializable {

	private static double[] upgradeTimes = new double[]{0.0, 0.0, 1.0, 1.5, 2.5}; // worker-days required to reach different levels
	private static int[] upgradeCosts = new int[]{0, 0, 36, 60, 100};
	private static int[] treasurySizes = new int[]{50, 75, 125, 200, 500}; // how much gold the capital can hold
	private static int[] housingCapacities = new int[]{8, 10, 16, 22, 32};
	private double nextSubjectProgress; // how close it is to producing another subject


	public Capital(int startingLevel) {
		level = startingLevel;
	}

	// ====================================

	protected void update(GameInstance instance, double progress) {
		nextSubjectProgress += progress * City.getNewPopPerDay(level) * instance.getPopGrowthModifier().getGrowthMod();
		if (nextSubjectProgress >= 1.0) {
			nextSubjectProgress = 0.0;
			instance.newSubjectAtSettlement(this);
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

			case CAPTAIN:
				return level >= 4;

			default: return false;
		}
	}

	// ====================================

	public void onSubjectAdded(SubjectType s, GameInstance instance) {
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

	// ======== ACCESSORS ================

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
		return 5;
	}

	public double getNewPopProgress() {
		return nextSubjectProgress;
	}

	public double getUpgradeConstructionTime() {
		if (level == getMaxLevel()) {
			return 0.0;
		}
		return upgradeTimes[level];
	}

	public int getUpgradeCost() {
		if (level == getMaxLevel()) {
			return 0;
		}
		return upgradeCosts[level];
	}

	public String toString() {
		return "Capital City";
	}


}