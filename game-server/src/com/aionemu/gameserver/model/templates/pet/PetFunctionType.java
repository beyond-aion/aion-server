package com.aionemu.gameserver.model.templates.pet;

/**
 * @author IlBuono, Rolandas
 */
public enum PetFunctionType {
	WAREHOUSE(0, true),
	FOOD(1, 64),
	DOPING(2, 256),
	LOOT(3, 8),

	APPEARANCE(1),
	NONE(4, true),

	// non writable to packets
	BAG(-1),
	WING(-2);

	private short id;
	private boolean isPlayerFunc = false;

	PetFunctionType(int id, boolean isPlayerFunc) {
		this(id);
		this.isPlayerFunc = isPlayerFunc;
	}

	PetFunctionType(int id, int dataBitCount) {
		this(dataBitCount << 5 | id);
		this.isPlayerFunc = true;
	}

	PetFunctionType(int id) {
		this.id = (short) (id & 0xFFFF);
	}

	public int getId() {
		return id;
	}

	public boolean isPlayerFunction() {
		return isPlayerFunc;
	}
}
