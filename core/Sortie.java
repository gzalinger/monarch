// A human attack which is in progress.

package core;
import java.io.Serializable;


public class Sortie implements Serializable {

	public static final double DISTANCE_PENALTY = 0.05; // percent strength penalty per extra distance from target
	private static final double BASE_CASAULTIES_PER_DAY = 5.2; // if a sortie fights all day, it will lose this many soldiers
	private MapNode location; // where it was launched from
	private Stronghold target;
	private int distanceToTarget;
	private int casaultyCount; // record of how many human soldiers have been killed
	private double victoryProgress; // how close human forces are to winning
	private double nextCasaultyProgress;


	public Sortie(MapNode node, Stronghold stronghold, GameInstance instance) {
		location = node;
		target = stronghold;
		distanceToTarget = Map.getDistance(instance.findLocationOfStronghold(target), location) - 1;
		// ^ (adjacent counts as zero distance)
	}

	// ================================

	public double getDistancePenalty() {
		return Math.min(1.0, distanceToTarget * DISTANCE_PENALTY);
	}

	// ================================

	public int calculateHumanStrength(GameInstance instance) {
		double strength = instance.getTotalSubjectsAtNode(location, SubjectType.SOLDIER) * GameInstance.DEFENSE_PER_SOLDIER;
		if (instance.getTotalSubjectsAtNode(location, SubjectType.CAPTAIN) > 0) {
			strength *= GameInstance.CAPTAIN_STRENGTH_BONUS;
		}

		strength = strength - strength * getDistancePenalty();
		return (int)Math.round(strength);
	}

	// ================================

	public void update(double progress, GameInstance instance) {
		victoryProgress += progress * calculateHumanStrength(instance) / target.getStrength(instance);
		if (victoryProgress >= 1.0) {
			instance.sortieVictory(this);
		}

		nextCasaultyProgress += progress * getCurrentCasaultyRate(instance);
		if (nextCasaultyProgress >= 1.0) {
			nextCasaultyProgress = 0.0;
			instance.inflictSortieCasaulty(this);
			casaultyCount++;
		}
	}

	// ================================

	private double getCurrentCasaultyRate(GameInstance instance) {
		return BASE_CASAULTIES_PER_DAY + 0.04 * target.getStrength(instance);
	}

	// ======== ACCESSORS =============

	public int getCasaultyCount() {
		return casaultyCount;
	}

	public double getCasaultyProgress() {
		return nextCasaultyProgress;
	}

	public MapNode getLocation() {
		return location;
	}

	public Stronghold getTarget() {
		return target;
	}

	public double getVictoryProgress() {
		return victoryProgress;
	}

}