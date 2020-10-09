// all professions a subject can have

package core;


public enum SubjectType {

	IDLE("Idle", true, 0),
	WORKER("Worker", true, 3),
	SOLDIER("Soldier", false, 2),
	FARMER("Farmer",true, 6),
	CAPTAIN("Captain", false, 20);


	private String displayName;
	private boolean isCivilian;
	private int baseEquipmentCost; // how much it costs to turn an idle citizen into this type


	private SubjectType(String s, boolean b, int i) {
		displayName = s;
		baseEquipmentCost = i;
		isCivilian = b;
	}

	// ============================

	public String toString() {
		return displayName;
	}

	// ============================

	public int getBaseEquipmentCost() {
		return baseEquipmentCost;
	}
	
	// ============================

	public boolean isCivilian() {
		return isCivilian;
	}
	

}