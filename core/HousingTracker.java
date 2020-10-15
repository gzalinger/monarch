// Object that tracks all elements of the kingdom's housing and does related calculations

package core;
import java.io.Serializable;


public class HousingTracker implements Serializable {

	private static final double HOUSING_PER_OPEN_NODE = 0.25; // housing capacity of a human-controlled node with no settlement
	private int numOpenNodes;
	private int housingFromCities;
	private int housingFromCapital;
	private int total;


	public void recalculate(GameInstance instance) {
		numOpenNodes = 0;
		housingFromCities = 0;
		housingFromCapital = 0;
		total = 0;

		// count open nodes:
		for (MapNode node: instance.getMap().getNodeList()) {
			if (instance.isNodeCaptured(node) && instance.getSettlementAt(node) == null) {
				numOpenNodes++;
			}
		}
		// cities:
		for (Settlement s: instance.getAllSettlements()) {
			if (s.isBuildingSite()) {
				continue;
			}

			if (s instanceof City) {
				housingFromCities += s.getHousingCapacity();
			}
			else if (s instanceof Capital) {
				housingFromCapital = s.getHousingCapacity();
			}
		}

		total = housingFromCities + housingFromCapital + getOpenNodeHousing();
	}

	// ==================================================

	public int getOpenNodeHousing() {
		return (int)Math.round(HOUSING_PER_OPEN_NODE * numOpenNodes);
	}

	// ================== ACCESSORS =====================

	public int getCapitalHousing() {
		return housingFromCapital;
	}

	public int getCitiesHousing() {
		return housingFromCities;
	}

	public int getTotal() {
		return total;
	}


}