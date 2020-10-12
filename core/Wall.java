// The static defenses at a node

package core;
import java.io.Serializable;


public class Wall implements Serializable {

	public static final int MAX_LEVEL = 8;
	private static int[] constructionAndUpgradeCosts = new int[]{4, 6, 16, 24, 40, 50, 50, 50};
	private static double[] constructionTimes = new double[]{0.20, 0.6, 1.2, 1.6, 2.5, 2.7, 3.0, 3.4};
	private static int[] defenseRatings = new int[]{6, 12, 24, 38, 54, 72, 88, 102};
	private int level;
	private boolean isBuildingSite;
	private boolean isUpgrading;
	private double constructionProgress;


	public Wall() {
		isBuildingSite = true;
		level = 1;
	}

	// ===================================

	public void update(double progress, GameInstance instance) {
		if (isBuildingSite || isUpgrading) {
			constructionProgress += progress * instance.getTotalSubjectsAtWall(this, SubjectType.WORKER) / constructionTimes[level - 1];
			if (constructionProgress >= 1.0) {
				isBuildingSite = false;
				if (isUpgrading) {
					isUpgrading = false;
					level++;
				}
				constructionProgress = 0.0;
			}
		}
	}

	// ===================================

	public void beginUpgrade() {
		isUpgrading = true;
		constructionProgress = 0.0;
	}

	// ========= ACCESSORS ===============

	public double getConstructionProgress() {
		return constructionProgress;
	}

	public static int getCost(int level) {
		if (level >= MAX_LEVEL) {
			return 0;
		}
		return constructionAndUpgradeCosts[level - 1];
	}

	public int getDefenseRating() {
		return defenseRatings[level - 1];
	}

	public int getLevel() {
		return level;
	}

	public int getUpgradeCost() {
		if (level >= MAX_LEVEL) {
			return 0;
		}
		return constructionAndUpgradeCosts[level];
	}

	public boolean isBuildingSite() {
		return isBuildingSite;
	}

	public boolean isUpgrading() {
		return isUpgrading;
	}


}