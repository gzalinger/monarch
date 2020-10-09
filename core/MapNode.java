// A single node on the map.

package core;
import java.io.Serializable;
import java.util.LinkedList;


public class MapNode implements Serializable {

	private MapNode parent; // null only for the root
	private double x;
	private double y; // coordinates in map space
	private LinkedList<MapNode> children;


	public MapNode(double _x, double _y) {
		children = new LinkedList<MapNode>();
		x = _x;
		y = _y;
	}

	// ===============================

	public void addChild(MapNode node) {
		node.parent = this;
		children.add(node);
	}

	// ===============================

	public LinkedList<MapNode> getNeighbors() {
		LinkedList<MapNode> list = new LinkedList<MapNode>();
		list.addAll(children);
		if (parent != null) {
			list.add(parent);
		}
		return list;
	}

	// ===============================

	public int getDistanceFromRoot() {
		MapNode temp = this;
		int distance = 0;
		while (temp.parent != null) {
			distance++;
			temp = temp.parent;
		}
		return distance;
	}

	// ===============================

	public boolean isLeaf() {
		return children.size() == 0;
	}

	// ========== ACCESSORS =============

	public LinkedList<MapNode> getChildren() {
		return children;
	}

	public MapNode getParent() {
		return parent;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}


}