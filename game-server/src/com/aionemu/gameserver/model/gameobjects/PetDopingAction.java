package com.aionemu.gameserver.model.gameobjects;

/**
 * Actions of the pet doping function, sent in form 13 subtype 2.
 */
public enum PetDopingAction {

	ADD(0),
	REMOVE(1),
	SWITCH(2),
	USE(3);

	private final int id;

	PetDopingAction(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static PetDopingAction getById(int id) {
		for (PetDopingAction action : values()) {
			if (action.getId() == id)
				return action;
		}
		return null;
	}
}
