// NPC settlements that provide early game gold

package core;
import java.io.Serializable;


public class Peddler extends Settlement implements Serializable {

	private static final double GOLD_PER_DAY = 4.2;
	private double progressTowardsNextGold;


	public Peddler() {
		level = 1;
	}

	// ====================================

	protected void update(GameInstance instance, double progress) {
		// generate money:
		if (instance.isDaytime() && gold < getMaxGold()) {
			progressTowardsNextGold += progress * GOLD_PER_DAY;
			if (progressTowardsNextGold >= 1.0) {
				gold++;
				progressTowardsNextGold = 0.0;
			}
		}
	}

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

	// ======= ACCESSORS ==================

	public boolean allowsGoldDeposits() {
		return false;
	}

	public boolean doesSellEquipment(SubjectType s) {
		return false;
	}

	public int getHousingCapacity() {
		return 0;
	}

	public int getMaxGold() {
		return 6;
	}

	public int getMaxLevel() {
		return 1;
	}

	public double getProgress() {
		return progressTowardsNextGold;
	}

	public double getUpgradeConstructionTime() {
		return 0.0; // can't be upgraded, doesn't matter
	}

	public int getUpgradeCost() {
		return 0; // can't be upgraded, doesn't matter
	}

	public String toString() {
		return "Peddler";
	}
	

}