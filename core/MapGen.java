// Home of the logic for creating procedurally generated maps.

package core;
import java.awt.geom.Point2D.Double;
import java.util.LinkedList;


public class MapGen {

	private static final int MAX_LANE_DEPTH = 15;
	private static final int MIN_LANE_DEPTH = 8; // they can be shorter if they twist into dead ends
	private static final double LANE_END_CHANCE = 0.21; // this controls how long lanes tend to be between the limits above
	private static final double FORK_CHANCE = 0.17; // how often should a lane split?
	private static final double DOUBLE_FORK_CHANCE = 0.03; // how often should a lane split into 3 children?
	private static final double CORNER_CHANCE = 0.07; // how often lanes should change directions
	private static final int MIN_PEDDLER_DEPTH = 3;
	private static final int MAX_PEDDLER_DEPTH = 8;
	private static final double PEDDLER_CHANCE = 0.35; // chance of a peddler being located on each node between min and max
	private static final int MIN_STRONGHOLD_DEPTH = 7; // intemrediate strongholds shouldn't be closer than this
	private static final double STRONGHOLD_CHANCE = 0.1;
	private static final int MIN_ECON_SPACING = 2; // a 2 here means they can't be adjcanet
	private static final int MAX_ECON_SPACING = 12; // spacing between economic resources (farms and gold veins)
	private static final double ECON_FREQUENCY = 0.2; // frequency of econ resources among nodes
	private static final double ECON_PERCENTAGE_FARMS = 0.5; // percentage of econ resources which are farms



	public static Map createMap() {
		Map map = new Map();
		MapNode root = new MapNode(0, 0);
		map.setRootNode(root);
		
		int rootLanes = calculateNumRootLanes();
		LinkedList<MapNode> creationQueue = new LinkedList<MapNode>(); // list of nodes that still need their descendants worked out
		LinkedList<Double> rootLaneDirs = shuffle(getCardinalDirections());
		for (int i = 0; i < rootLanes; i++) {
			MapNode child = new MapNode(rootLaneDirs.get(i).getX(), rootLaneDirs.get(i).getY());
			root.addChild(child);
			creationQueue.add(child);
		}

		// start recursing on creationQueue:
		while (creationQueue.size() > 0) {
			generateFromNode(map, creationQueue.removeFirst(), creationQueue);
		}
		map.makeNodeList(); // map is now fully built

		// put some stuff on it:
		addPeddlers(map);
		addStrongholds(map);
		addFarmsAndGoldVeins(map);

		return map;
	}

	// ========================================

	private static void generateFromNode(Map map, MapNode node, LinkedList<MapNode> queue) {
		// create children of this node and add them to the back of the queue
		int depth = Map.getDistance(node, map.getRootNode());

		// First, determine if this is a leaf:
		if (depth >= MAX_LANE_DEPTH) {
			return;
		}
		else if (depth > MIN_LANE_DEPTH && Math.random() < LANE_END_CHANCE) {
			return;
		}

		int numChildren = calculateNumChildrenForNode();
		for (int i = 0; i < numChildren; i++) {
			Double loc = findLocationForChild(map, node); // looks for a valid location
			if (loc != null) {
				MapNode child = new MapNode(loc.getX(), loc.getY());
				node.addChild(child);
				queue.add(child);
			}
		}
	}

	// ========================================

	private static Double findLocationForChild(Map map, MapNode node) {
		// Determine where a child can/should be, return null if there is no space
		LinkedList<Double> directions = shuffle(getCardinalDirections());

		// remove direction that parent is in:
		Double parentDir = new Double(node.getParent().getX() - node.getX(), node.getParent().getY() - node.getY());
		for (int i = 0; i < 4; i++) {
			if (directions.get(i).getX() == parentDir.getX() && directions.get(i).getY() == parentDir.getY()) {
				directions.remove(i);
				break;
			}
		}

		Double straightDir = null;
		for (Double d: directions) {
			if (d.getX() == -1 * parentDir.getX() && d.getY() == -1 * parentDir.getY()) {
				straightDir = d;
				break;
			}
		}
		if (straightDir == null) {
			System.err.println("ERROR: could not find straight line direction (" 
				+ node.getX() + ", " + node.getY() + "; " + parentDir.getX() + ", " + parentDir.getY() + "; " + node.getParent().getX() + "," + node.getParent().getY() + ")");
		}

		// determine priority order of remaining dirs, based on corner chance
		if (Math.random() < CORNER_CHANCE) {
			// try to turn
			directions.remove(straightDir);
			directions.add(straightDir); // this moves straight direction to the end of pri queue
		}
		else {
			// prioritize going straight ahead:
			directions.remove(straightDir);
			directions.addFirst(straightDir);
		}

		// remove any directions that collide with existing nodes:
		LinkedList<Double> dirCopy = new LinkedList<Double>();
		dirCopy.addAll(directions);
		for (Double d: dirCopy) {
			if (map.findNodeAtLocation(node.getX() + d.getX(), node.getY() + d.getY()) != null) {
				directions.remove(d);
			}
		}

		if (directions.size() == 0) {
			// There are no valid places to put children, so this must be a dead end:
			return null;
		}

		// return highest pri remaining dir:
		Double dir = directions.getFirst();
		return new Double(node.getX() + dir.getX(), node.getY() + dir.getY());
	}

	// ========================================

	private static int calculateNumRootLanes() {
		// Decide how many lanes should radiate from root
		double r = Math.random();
		if (r < 0.7) {
			return 2;
		}
		else if (r < 0.97) {
			return 3;
		}
		else {
			return 4;
		}
	}

	// ========================================

	private static LinkedList<Double> getCardinalDirections() {
		LinkedList<Double> list = new LinkedList<Double>();
		list.add(new Double(1, 0));
		list.add(new Double(-1, 0));
		list.add(new Double(0, 1));
		list.add(new Double(0, -1));
		return list;
	}

	// ========================================

	private static LinkedList<Double> shuffle(LinkedList<Double> list) {
		for (int i = 0; i < list.size() * 2; i++) {
			int r = (int)(Math.random() * list.size());
			list.add(list.remove(r));
		}
		return list;
	}

	// ========================================

	private static int calculateNumChildrenForNode() {
		// How many children should some random node try to have, assuming there is space for them:
		if (Math.random() < DOUBLE_FORK_CHANCE) {
			return 3;
		}
		if (Math.random() < FORK_CHANCE) {
			return 2;
		}
		return 1;
	}

	// ========================================

	private static void addPeddlers(Map map) {
		int numPeddlers = 3 + (int)(Math.random() * 3);
		int numRootLanes = map.getRootNode().getChildren().size();
		int i = 0;
		// Apportion the peddlers evenly along the lanes:
		for (MapNode rootChild: map.getRootNode().getChildren()) {
			int n = numPeddlers / numRootLanes;
			if ((numPeddlers % numRootLanes) - i > 0) {
				n++;
			}
			if (n > 0) {
				placePeddlersOnLane(map, rootChild, n);
			}
			i++;
		}
	}

	// ========================================

	private static void placePeddlersOnLane(Map map, MapNode node, int numPeddlers) {
		// If we've hit a leaf, stop.
		if (node.isLeaf()) {
			return;
		}
		// First, determine if a peddler should go at this node:
		int depth = Map.getDistance(node, map.getRootNode());
		if (depth >= MIN_PEDDLER_DEPTH) {
			if (depth >= MAX_PEDDLER_DEPTH || Math.random() < PEDDLER_CHANCE) {
				map.addPeddlerLocation(node);
				numPeddlers--;
				if (numPeddlers <= 0) {
					return;
				}
			}
		}

		// Next, recurse and apportion peddlers across forks:
		int numChildren = node.getChildren().size();
		int i = 0;
		for (MapNode child: node.getChildren()) {
			int n = numPeddlers / numChildren;
			if ((numPeddlers % numChildren) - i > 0) {
				n++;
			}
			if (n > 0) {
				placePeddlersOnLane(map, child, n);
			}
			i++;
		}
	}

	// ========================================

	private static void addStrongholds(Map map) {
		// places intermediate strongholds on the map
		LinkedList<MapNode> leaves = map.getLeaves();
		int numStrongholds = (int)Math.round(leaves.size() * (0.25 +Math.random() * 0.5));
		for (int i = 0; i < numStrongholds; i++) {
			placeStrongholdOnLane(map, leaves.get(i).getParent());
		}
	}

	// ========================================

	private static void placeStrongholdOnLane(Map map, MapNode node) {
		// put an intermeidate stronghold at this node or among its ancestors
		int depth = map.getDistance(node, map.getRootNode());
		if ((depth <= MIN_STRONGHOLD_DEPTH || Math.random() < STRONGHOLD_CHANCE) && !map.getPeddlerLocations().contains(node)) {
			map.addStrongholdLocation(node);
		}
		else {
			placeStrongholdOnLane(map, node.getParent());
		}
	}

	// ========================================

	private static void addFarmsAndGoldVeins(Map map) {
		// Both are placed at the same time b/c they fill similar roles
		for (MapNode child: map.getRootNode().getChildren()) {
			addFarmsAndGoldVeinsToLane(map, child, 1);
		}

		// make sure there is at least one farm or gold vein within 4 nodes of the root:
		boolean foundOne = false;
		LinkedList<MapNode> nearbyNodes = new LinkedList<MapNode>();
		LinkedList<MapNode> searchQueue = new LinkedList<MapNode>();
		searchQueue.addAll(map.getRootNode().getChildren());
		while (searchQueue.size() > 0) {
			MapNode search = searchQueue.removeFirst();
			if (map.getArableLand().contains(search) || map.getGoldVeins().contains(search)) {
				foundOne = true;
				break;
			}
			if (!map.getStrongholdLocations().contains(search) && !map.getPeddlerLocations().contains(search)) {
				nearbyNodes.add(search);
			}
			int dist = Map.getDistance(search, map.getRootNode());
			if (dist < 4) {
				searchQueue.addAll(search.getChildren());
			}
		}
		if (!foundOne) {
			int r = (int)(Math.random() * nearbyNodes.size());
			if (Math.random() < ECON_PERCENTAGE_FARMS) {
				map.addArableLand(nearbyNodes.get(r));
			}
			else {
				map.addGoldVein(nearbyNodes.get(r));
			}
		}
	}

	// ========================================

	private static void addFarmsAndGoldVeinsToLane(Map map, MapNode node, int depthFromLastEcon) {
		// First, determine if an econ resources will be placed here:
		boolean placed = false;
		if (node.getChildren().size() > 0 && !map.getStrongholdLocations().contains(node) && !map.getPeddlerLocations().contains(node)) {
			if (depthFromLastEcon >= MAX_ECON_SPACING) {
				placed = true;
				if (Math.random() < ECON_PERCENTAGE_FARMS) {
					map.addArableLand(node);
				}
				else {
					map.addGoldVein(node);
				}
			}
			else if (depthFromLastEcon > MIN_ECON_SPACING) {
				if (Math.random() <= ECON_FREQUENCY) {
					placed = true;
					if (Math.random() < ECON_PERCENTAGE_FARMS) {
						map.addArableLand(node);
					}
					else {
						map.addGoldVein(node);
					}
				}
			}
		}

		// Now recurse.
		for (MapNode child: node.getChildren()) {
			addFarmsAndGoldVeinsToLane(map, child, placed ? 1 : depthFromLastEcon + 1);
		}
	}


}