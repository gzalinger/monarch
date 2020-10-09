// An enemy settlement. Blocks player expansion and must be destroyed with a sortie.
// Can be either at a leaf or in the middle of the map.

package core;
import java.io.Serializable;


public class Stronghold implements Serializable {


	private int distanceFromCapital; // this affects how strong it is
	private boolean isIntermediate; // if not, it is at a leaf of the map tree


	public Stronghold(MapNode location) {
		distanceFromCapital = location.getDistanceFromRoot();
		isIntermediate = !location.isLeaf();
	}

	// ====================================

	public int getStrength(GameInstance instance) {
		double strength = 10.0 + 0.8 * instance.getDangerLevel();
		// +5% for every node from capital:
		strength *= (1.0 + 0.05 * distanceFromCapital);

		// intermediate ones are weaker:
		if (isIntermediate) {
			strength *= 0.75;
		}

		return (int)Math.max(1, Math.round(strength));
	}	


}