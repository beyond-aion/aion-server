package com.aionemu.gameserver.model.templates.housing;

/**
 * @author Rolandas
 */
public enum HouseType {
	ESTATE(0, 3, "a"),
	MANSION(1, 2, "b"),
	HOUSE(2, 1, "c"),
	STUDIO(3, 0, "d"),
	PALACE(4, 4, "s");

	private final int limitTypeIndex;
	private final int id;
	private final String abbrev; // building parts end with this letter (like CP_S for palace)

	HouseType(int index, int id, String abbrev) {
		this.limitTypeIndex = index;
		this.id = id;
		this.abbrev = abbrev;
	}

	public int getLimitTypeIndex() {
		return limitTypeIndex;
	}

	public int getId() {
		return id;
	}

	public String getAbbreviation() {
		return abbrev;
	}
}
