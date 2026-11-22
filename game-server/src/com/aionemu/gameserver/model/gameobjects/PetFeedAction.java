package com.aionemu.gameserver.model.gameobjects;

/**
 * Subtypes of the pet feeding function, sent in form 9.
 */
public enum PetFeedAction {

	START(1),
	EATEN(2),
	STOPPED(3),
	CANCEL(4),
	FINISHED(5),
	GAVE_ITEM(6),
	REFEED(7),
	REJECTED(8);

	private final int id;

	PetFeedAction(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
