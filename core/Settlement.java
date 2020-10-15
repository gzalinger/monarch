// Covers any kind of (friendly) development at a map node.
// Note that this does not cover fortifications.

package core;
import java.io.Serializable;


public abstract class Settlement implements Serializable {

	protected int level; // upgrade level
	protected int gold; // amount in the treasury
	protected boolean isBuildingSite; // if true, settlement has not yet been built (even at level 1)
	protected double constructionProgress; // percentage complete that building or upgrading is
	protected boolean isUpgrading;


	public abstract boolean allowsGoldDeposits();


	public abstract int getMaxGold();


	public abstract boolean doesSellEquipment(SubjectType s);


	protected abstract void update(GameInstance parent, double progress);


	public abstract void onSubjectAdded(SubjectType s, GameInstance instance);


	public abstract void onSubjectSubtracted(SubjectType s, GameInstance instance);


	public abstract int getMaxLevel();


	public abstract int getUpgradeCost();


	public abstract double getUpgradeConstructionTime();


	protected abstract void onUpgradeFinished(GameInstance instance);


	public abstract int getHousingCapacity();


	// =====================================

	public void beginUpgrade() {
		isUpgrading = true;
		constructionProgress = 0.0;
	}

	// =====================================

	public void updateSettlement(GameInstance parent, double progress) {
		if (isUpgrading) {
			constructionProgress += progress * parent.getTotalSubjectsAtSettlement(this, SubjectType.WORKER) / getUpgradeConstructionTime();
			if (constructionProgress >= 1.0) {
				constructionProgress = 0.0;
				isUpgrading = false;
				level++;
				onUpgradeFinished(parent);
				parent.onSettlementUpgradeFinished(this);
			}
		}

		update(parent, progress);
	}

	// ============ ACCESSORS ==============

	public void addGold(int n) {
		gold += n;
	}

	public double getConstructionProgress() {
		return constructionProgress;
	}

	public int getGold() {
		return gold;
	}

	public int getLevel() {
		return level;
	}

	public boolean isBuildingSite() {
		return isBuildingSite;
	}

	public boolean isUpgrading() {
		return isUpgrading;
	}


}