// Basically a wrapper for the scaler difficulty and a name

package core;


public enum DifficultyLevel {


	TRIVIAL("Trivial", 0.75),
	EASY("Easy", 1.0),
	MODERATE("Moderate", 1.3),
	HARD("Hard", 1.5),
	INSANE("Insane", 2.0);


	private String displayName;
	private double dailyIncrease;


	private DifficultyLevel(String s, double d) {
		displayName = s;
		dailyIncrease = d;
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
		return dailyIncrease;
	}

}