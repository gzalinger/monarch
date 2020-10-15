// A settlement that employs farmers and produces gold

package core;
import java.io.Serializable;
import java.util.ArrayList;


public class Farm extends Settlement implements Serializable {

	private static int[] buildingAndUpgradingCosts = new int[]{10, 12, 18};
	private static double[] constructionTimes = new double[]{0.5, 1.0, 1.5}; // unit is worker-days
	private static int[] maxGold = new int[]{25, 32, 40};
	private static int[] maxFarmers = new int[]{3, 4, 5}; // how many can be employed at once
	private static int[] harvestYields = new int[]{5, 6, 7}; // amount of gold per farmer when their farming is done
	private ArrayList<Double> cropProgress; // tracks progress of each "field" towards its harvest


	public Farm() {
		isBuildingSite = true;
		level = 1;
		cropProgress = new ArrayList<Double>();
	}

	// ========================================

	protected void update(GameInstance instance, double progress) {
		if (isBuildingSite) {
			double newProgress = progress * instance.getTotalSubjectsAtSettlement(this, SubjectType.WORKER) / constructionTimes[0];
			constructionProgress += newProgress;
			if (constructionProgress >= 1.0) {
				constructionProgress = 0.0;
				isBuildingSite = false;
				assignFarmersToFields(instance);
				instance.onBuildingSiteFinished(this);
			}
		}
		else {
			// update crop progress:
			double newProgress = progress / getHarvestTime();
			for(int i = 0; i < cropProgress.size(); i++) {
				if (cropProgress.get(i) != null) {
					double newValue = cropProgress.get(i).doubleValue() + newProgress;
					if (newValue >= 1.0) {
						harvest(instance);
						newValue = 0.0;
					}
					cropProgress.set(i, new Double(newValue));

				}
			}
		}
	}

	// ========================================

	private void harvest(GameInstance instance) {
		// adds gold from a harvest
		int n = getHarvestYield();
		if (gold + n > getMaxGold()) {
			int excess = gold + n - getMaxGold();
			gold = getMaxGold();
			instance.depositExcessFromFarm(this, excess);
		}
		else {
			gold += n;
		}
	}

	// ========================================

	private void assignFarmersToFields(GameInstance instance) {
		// Called when the farm is completed. Takes any farmers who were already at the node and starts crops going for them:
		int numFarmers = instance.getPopulationAtNode(instance.findLocationOfSettlement(this), SubjectType.FARMER);	
		// don't count farmers who already ahve fields:
		numFarmers -= cropProgress.size();
		for (int i = 0; i < numFarmers && i < getMaxFarmers(); i++) {
			cropProgress.add(new Double(0.0));
		}
	}

	// ========================================

	public static int getCost(int level) {
		// returns the cost of upgrading to the given level
		return buildingAndUpgradingCosts[level - 1];
	}

	// ========================================

	public void onSubjectAdded(SubjectType s, GameInstance instance) {
		if (s == SubjectType.FARMER && !isBuildingSite) {
			if (cropProgress.size() < getMaxFarmers()) {
				cropProgress.add(new Double(0.0));
			}
			// else all fields are already being used, adding this farmer does nothing
		}
	}

	// ========================================

	public void onSubjectSubtracted(SubjectType s, GameInstance instance) {
		if (s == SubjectType.FARMER) {
			if (instance.getPopulationAtNode(instance.findLocationOfSettlement(this), SubjectType.FARMER) < cropProgress.size()) {
				// We know that the total number of farmers remaining is less than the number of active fields
				// So, remove the lowest progress field:
				Double lowest = null;
				for (Double d: cropProgress) {
					if (lowest == null || lowest.doubleValue() > d.doubleValue()) {
						lowest = d;
					}
				}
				cropProgress.remove(lowest);
			}
		}
	}

	// ========================================

	protected void onUpgradeFinished(GameInstance instance) {
		assignFarmersToFields(instance);
	}

	// =========== ACCESSORS ===================

	public boolean allowsGoldDeposits() {
		return false;
	}

	public boolean doesSellEquipment(SubjectType s) {
		return false;
	}

	public static double getHarvestTime() {
		// how many days between harvests
		return 1.475;
	}

	public Double getCropProgress(int idx) {
		if (idx < 0 || idx >= cropProgress.size()) {
			return null;
		}
		return cropProgress.get(idx);
	}

	public int getHarvestYield() {
		return harvestYields[level - 1];
	}

	public int getHousingCapacity() {
		return 0;
	}

	public int getMaxFarmers() {
		return maxFarmers[level - 1];
	}

	public int getMaxGold() {
		return maxGold[level - 1];
	}

	public int getMaxLevel() {
		// the max levels farms can reach in this game
		return 3;
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
		return "Farm";
	}


}