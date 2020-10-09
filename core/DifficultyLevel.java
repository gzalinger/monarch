// Basically a wrapper for the scaler difficulty and a name

package core;


public enum DifficultyLevel {


	EASY("Easy", 0.9, 1.35);


	private String displayName;
	private double earlyDailyIncrease; // how much danger level goes up each day (in the early game)
	private double midDailyIncrease;


	private DifficultyLevel(String s, double d1, double d2) {
		displayName = s;
		earlyDailyIncrease = d1;
		midDailyIncrease = d2;
	}

	// ====================================

	public static DifficultyLevel getDefault() {
		return EASY;
	}

	// ====================================

	public String toString() {
		return displayName;
	}

	// ====================================

	public double getDailyDangerIncrease(int dayCount) {
		if (dayCount <= 5) {
			return earlyDailyIncrease;
		}
		else {
			return midDailyIncrease;
		}
	}

}