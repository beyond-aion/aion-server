package com.aionemu.gameserver.model.gameobjects;

/**
 * Actions on a pet function, sent by the client in the variable part of {@code C_FUNCTIONAL_PET} action 9.
 */
public enum PetFunctionAction {

	FEEDING(1),
	DOPING(2),
	AUTOLOOT(3),
	AUTOSELL(4),
	BUFF(5);

	private final int id;

	PetFunctionAction(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static PetFunctionAction getById(int id) {
		for (PetFunctionAction action : values()) {
			if (action.getId() == id)
				return action;
		}
		return null;
	}
}
