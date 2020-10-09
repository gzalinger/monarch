// Wrapper for two nodes.

package core;
import java.io.Serializable;


public class MapEdge implements Serializable {

	private MapNode nodeA;
	private MapNode nodeB;


	public MapEdge(MapNode a, MapNode b) {
		nodeA = a;
		nodeB = b;
	}

	// =============================

	public MapNode getA() {
		return nodeA;
	}

	public MapNode getB() {
		return nodeB;
	}

}