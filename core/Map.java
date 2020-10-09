// Contains all info about a game, nothing about a particular game instance.

package core;
import java.awt.geom.Point2D.Double;
import java.io.Serializable;
import java.util.LinkedList;


public class Map implements Serializable {

	private static final Map defaultMap;
	private MapNode root;
	private LinkedList<MapNode> nodeList; // list of all nodes w/o tree structure for easy iteration
	private LinkedList<MapNode> intermediateStrongholdLocations; 
	private LinkedList<MapNode> peddlerLocations;
	private LinkedList<MapNode> arableLand;
	private LinkedList<MapNode> goldVeins;


	static {
		defaultMap = new Map();
		defaultMap.root = new MapNode(0.0, 0.0);
		MapNode node1 = new MapNode(-1.0, 0);
		defaultMap.root.addChild(node1);
		MapNode node2 = new MapNode(-2.0, 0);
		node1.addChild(node2);
		node1 = node2;
		node2 = new MapNode(-3.0, 0);
		node1.addChild(node2);
		node1 = node2;
		MapNode fork = new MapNode(-4.0, 0);
		node1.addChild(fork);
		defaultMap.peddlerLocations.add(fork);
		node1 = new MapNode(-4, -1);
		fork.addChild(node1);
		node2 = new MapNode(-4, -2);
		node1.addChild(node2);
		node1 = node2;
		node2 = new MapNode(-5, -2);
		node1.addChild(node2);
		node1 = node2;
		node2 = new MapNode(-6, -2);
		node1.addChild(node2);
		// -----
		node1 = new MapNode(-4, 1);
		fork.addChild(node1);
		node2 = new MapNode(-4, 2);
		node1.addChild(node2);
		node1 = node2;
		node2 = new MapNode(-4, 3);
		node1.addChild(node2);
		node1 = node2;
		node2 = new MapNode(-4, 4);
		node1.addChild(node2);
		// ----
		fork = new MapNode(1, 0);
		defaultMap.root.addChild(fork);
		node1 = new MapNode(1, 1);
		fork.addChild(node1);
		node2 = new MapNode(1, 2);
		node1.addChild(node2);
		defaultMap.peddlerLocations.add(node2);
		node1 = node2;
		node2 = new MapNode(1, 3);
		node1.addChild(node2);
		MapNode fork2 = new MapNode(1, 4);
		defaultMap.intermediateStrongholdLocations.add(fork2);
		node2.addChild(fork2);
		node2 = new MapNode(1, 5);
		fork2.addChild(node2);
		node1 = new MapNode(0, 4);
		defaultMap.peddlerLocations.add(node1);
		fork2.addChild(node1);
		node2 = new MapNode(-1, 4);
		node1.addChild(node2);
		node1 = node2;
		node2 = new MapNode(-1, 5);
		node1.addChild(node2);
		// ------
		node1 = new MapNode(2, 0);
		fork.addChild(node1);
		node2 = new MapNode(3, 0);
		node1.addChild(node2);
		defaultMap.peddlerLocations.add(node2);
		fork = new MapNode(4, 0);
		node2.addChild(fork);
		node1 = new MapNode(4, -1);
		fork.addChild(node1);
		node2 = new MapNode(4, -2);
		node1.addChild(node2);
		// ------
		node1 = new MapNode(5, 0);
		fork.addChild(node1);
		node2 = new MapNode(6, 0);
		node1.addChild(node2);


		defaultMap.makeNodeList();
	}

	// =========================================

	public Map() {
		intermediateStrongholdLocations = new LinkedList<MapNode>();
		peddlerLocations = new LinkedList<MapNode>();
		arableLand = new LinkedList<MapNode>();
		goldVeins = new LinkedList<MapNode>();
	}

	// =========================================

	public static Map getDefaultMap() {
		return defaultMap;
	}

	// =========================================

	public static MapNode findNeighborInDirection(MapNode source, Double direction) {
		double dirAngle = findAngleTo(new Double(0, 0), direction);
		
		// Find the neighbor that is closest to that angle (and that's within 90):
		LinkedList<MapNode> neighbors = new LinkedList<MapNode>();
		neighbors.addAll(source.getChildren());
		if (source.getParent() != null) {
			neighbors.add(source.getParent());
		}
		MapNode bestNode = null;
		double bestAngle = 0.0; // angle from source to bestNode
		for (MapNode n: neighbors) {
			double angle = findAngleTo(new Double(source.getX(), source.getY()), new Double(n.getX(), n.getY()));
			double angleDiff = Math.abs(angle - dirAngle);
			if (angleDiff >= 90) {
				continue;
			}
			if (bestNode == null || angleDiff < Math.abs(bestAngle - dirAngle)) {
				bestNode = n;
				bestAngle = angle;
			}
		}

		return bestNode;
	}

	// =========================================

	public static double findAngleTo(Double a, Double b) {
		double angle = Math.toDegrees(Math.atan2(b.getX() - a.getX(), b.getY() - a.getY()));
		if (angle > 359.0) {
			angle = 359.0;
		}
		if (angle < 0) {
			angle += 360;
		}
		return angle;
	}

	// =========================================

	public static double findAngleTo(MapNode a, MapNode b) {
		double angle = Math.toDegrees(Math.atan2(b.getX() - a.getX(), b.getY() - a.getY()));
		if (angle > 359.0) {
			angle = 359.0;
		}
		if (angle < 0) {
			angle += 360;
		}
		return angle;
	}

	// =========================================

	public static MapNode findDestinationOfMovement(MapEdge edge, Double direction) {
		// Determines which end of the edge the direction points in.
		if (direction.getX() < 0) {
			if (edge.getA().getX() > edge.getB().getX()) {
				return edge.getB();
			}
			else {
				return edge.getA();
			}
		}
		if (direction.getX() > 0) {
			if (edge.getA().getX() > edge.getB().getX()) {
				return edge.getA();
			}
			else {
				return edge.getB();
			}
		}
		if (direction.getY() < 0) {
			return edge.getA().getY() < edge.getB().getY() ? edge.getA() : edge.getB();
		}
		if (direction.getY() > 0) {
			return edge.getA().getY() > edge.getB().getY() ? edge.getA() : edge.getB();
		}

		return null;
	}

	// =========================================

	public static double getAngleOfMovement(MapEdge edge, Double direction) {
		// Get angle in degrees that monarch is moving at along this edge
		// direction parameter is [-1, 1] stuff
		MapNode towards = findDestinationOfMovement(edge, direction);
		MapNode from = (towards == edge.getA()) ? edge.getB() : edge.getA();

		return findAngleTo(from, towards);
	}

	// =========================================

	public static double calculateDistance(Double a, Double b) {
		return Math.sqrt( (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY()) );
	}

	// =========================================

	public void makeNodeList() {
		nodeList = new LinkedList<MapNode>();
		addToNodeList(root);
	}

	private void addToNodeList(MapNode n) {
		nodeList.add(n);
		for (MapNode c: n.getChildren()) {
			addToNodeList(c);
		}
	}

	// =========================================

	public LinkedList<MapNode> getLeaves() {
		LinkedList<MapNode> list = new LinkedList<MapNode>();
		for (MapNode node: nodeList) {
			if (node.isLeaf()) {
				list.add(node);
			}
		}
		return list;
	}

	// =========================================

	public static int getDistance(MapNode a, MapNode b) {
		int distance = 0;
		MapNode temp = a;
		while (temp != b) {
			distance++;
			if (temp.getParent() == null) {
				// a was not a descendant of b...try the other way:
				return getDistance(b, a);
			}
			temp = temp.getParent();
		}
		return distance;
	}

	// =========================================

	public MapNode findNodeAtLocation(double x, double y) {
		// NOTE that this cannot rely on the node list having been built
		return findNodeAtLocation(root, x, y);
	}

	private MapNode findNodeAtLocation(MapNode node, double x, double y) {
		if (node.getX() == x && node.getY() == y) {
			return node;
		}

		for (MapNode child: node.getChildren()) {
			MapNode found = findNodeAtLocation(child, x, y);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	// =============== ACCESSORS ================

	public void addArableLand(MapNode node) {
		arableLand.add(node);
	}

	public void addGoldVein(MapNode node) {
		goldVeins.add(node);
	}

	public void addPeddlerLocation(MapNode node) {
		peddlerLocations.add(node);
	}

	public void addStrongholdLocation(MapNode node) {
		intermediateStrongholdLocations.add(node);
	}

	public LinkedList<MapNode> getArableLand() {
		return arableLand;
	}

	public LinkedList<MapNode> getGoldVeins() {
		return goldVeins;
	}

	public LinkedList<MapNode> getNodeList() {
		return nodeList;
	}

	public LinkedList<MapNode> getPeddlerLocations() {
		return peddlerLocations;
	}

	public MapNode getRootNode() {
		return root;
	}

	public LinkedList<MapNode> getStrongholdLocations() {
		return intermediateStrongholdLocations;
	}

	public void setRootNode(MapNode node) {
		root = node;
	}


}