package com.aionemu.gameserver.model.templates.pet;

/**
 * @author IlBuono, Rolandas
 */
public enum PetFunctionType {
	WAREHOUSE(0),
	FOOD(1),
	DOPING(2),
	LOOT(3),
	BUFF(4),
	MERCHANT(5),
	NONE(6),

	APPEARANCE(1, false),

	// non writable to packets
	BAG(-1, false),
	WING(-2, false);

	private final byte id;
	private final boolean isPlayerFunc;

	PetFunctionType(int id) {
		this(id, true);
	}

	PetFunctionType(int id, boolean isPlayerFunc) {
		this.id = (byte) id;
		this.isPlayerFunc = isPlayerFunc;
	}

	public byte getId() {
		return id;
	}

	public boolean isPlayerFunction() {
		return isPlayerFunc;
	}
}
