// Wrapper for hashmap with tracker for total population

package core;
import java.io.Serializable;
import java.util.HashMap;


public class Population implements Serializable {

	private HashMap<SubjectType, Integer> subjects;
	private int total;


	public Population() {
		subjects = new HashMap<SubjectType, Integer>();
		for (SubjectType s: SubjectType.values()) {
			subjects.put(s, 0);
		}
	}

	// ====================================

	public static SubjectType getWeightedRandomSubject(MapNode node, GameInstance instance) {
		// picks a random subject type present in this pop, taking into account how many of each are here
		if (instance.getPopCountAtNode(node) == 0) {
			return null;
		}
		int r = (int)Math.ceil(Math.random() * instance.getPopCountAtNode(node));
		for (SubjectType st: SubjectType.values()) {
			int n = instance.getPopulationAtNode(node, st);
			if (n != 0 && n <= r) {
				return st;
			}
			r -= n;
		}
		System.out.println("ERROR: getWeightedRandomSubject is returning null (r = " + r + ")");
		return null; // shouldn't ever get to this point
	}

	// ====================================

	public void addSubject(SubjectType st) {
		subjects.put(st, subjects.get(st).intValue() + 1);
		total++;
	}

	// ====================================

	public void subtractSubject(SubjectType st) {
		subjects.put(st, subjects.get(st).intValue() - 1);
		total--;
	}

	// ====================================

	public int getCount(SubjectType s) {
		return subjects.get(s).intValue();
	}

	// ====================================

	public int getTotal() {
		return total;
	}

	// ====================================

	public void addAll(Population other) {
		for (SubjectType st: SubjectType.values()) {
			total += other.subjects.get(st).intValue();
			subjects.put(st, subjects.get(st).intValue() + other.subjects.get(st).intValue());
		}
	}

	// ====================================

	public void subtractAll(Population other) {
		for (SubjectType st: SubjectType.values()) {
			total -= other.subjects.get(st).intValue();
			subjects.put(st, subjects.get(st).intValue() - other.subjects.get(st).intValue());
		}
	}

}