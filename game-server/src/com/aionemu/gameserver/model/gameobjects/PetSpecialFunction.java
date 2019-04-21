package com.aionemu.gameserver.model.gameobjects;

/**
 * @author Neon
 */
public enum PetSpecialFunction {

	DOPING(2),
	AUTOLOOT(3),
	AUTOSELL(4);

	private final int id;

	PetSpecialFunction(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static PetSpecialFunction getById(int id) {
		for (PetSpecialFunction specialFunction : values()) {
			if (specialFunction.getId() == id)
				return specialFunction;
		}
		return null;
	}
}
