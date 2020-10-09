// An instance of an attack against a single node on a single night

package core;
import java.io.Serializable;


public class FrontierAttack implements Serializable {

	private MapNode node;
	private int totalDanger;
	private double dangerSoFar; // this increases over the course of the night until it hits totalDanger


	public FrontierAttack(MapNode n, int d) {
		node = n;
		totalDanger = d;
	}

	// =============================

	public void update(double progress) {
		dangerSoFar += progress * totalDanger;
	}

	// =============================

	public MapNode getTargetNode() {
		return node;
	}

	// =============================

	public double getCurrentStrength() {
		return dangerSoFar;
	}

	// =============================

	public int getTotalDanger() {
		return totalDanger;
	}

	// =============================

	public void addTotalDanger(int n) {
		// this happens when an attack overwhelms an attack at a child node and its remaining strength goes here
		totalDanger += n;
	}


}