package com.aionemu.gameserver.model.gameobjects;

/**
 * Subtypes of the pet mood function, sent in form 12.
 */
public enum PetMoodAction {

	STATUS(0),
	SOCIAL(2),
	PRESENT(3),
	UPDATE(4);

	private final int id;

	PetMoodAction(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
