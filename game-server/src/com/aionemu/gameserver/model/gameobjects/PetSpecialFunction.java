package com.aionemu.gameserver.model.gameobjects;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

public enum PetSpecialFunction {
	DOPING(2),
	AUTOLOOT(3),
	AUTOSELL(4),
	BUFF(5);

	private final int id;

	PetSpecialFunction(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@SuppressWarnings("unused")
	public static PetSpecialFunction getById(int id) {
		for (var specialFunction : values()) {
			if (specialFunction.getId() == id) {
				return specialFunction;
			}
		}
		return null;
	}
}