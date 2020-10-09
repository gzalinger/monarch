// A modifier that gets applied to population growth (new subjects at cities) based on housing level

package core;
import java.awt.Color;


public enum PopGrowthModifier {

	GREEN(Color.GREEN, 1.0, 0.0),
	YELLOW(Color.YELLOW, 0.8, 0.75),
	RED(Color.RED, 0.5, 1.0),
	BLACK(Color.BLACK, 0.05, 1.2);


	private Color color;
	private double growthMod; // gets applied to growth rate
	private double housingThreshold; // mod applies when pop-to-housing ratio exceeds this number


	private PopGrowthModifier(Color c, double d1, double d2) {
		color = c;
		growthMod = d1;
		housingThreshold = d2;
	}

	// ============================================

	public static PopGrowthModifier getCurrentModifier(double ratio) {
		// NOTE that this depends on values() returning them in ascendding order
		PopGrowthModifier mod = null;
		for (PopGrowthModifier m: values()) {
			if (ratio > m.housingThreshold) {
				mod = m;
			}
		}
		return mod;
	}

	// ============ ACCESSORS =====================

	public Color getColor() {
		return color;
	}

	public double getGrowthMod() {
		return growthMod;
	}


}