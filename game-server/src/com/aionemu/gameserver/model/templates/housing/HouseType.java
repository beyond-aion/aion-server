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

	private HouseType(int index, int id, String abbrev) {
		this.abbrev = abbrev;
		this.limitTypeIndex = index;
		this.id = id;
	}

	private String abbrev;
	private int limitTypeIndex;
	private int id; 

	public String getAbbreviation() {
		return abbrev;
	}
	
	public int getLimitTypeIndex() {
		return limitTypeIndex;
	}
	
	public int getId() {
		return id;
	}

	public String value() {
		return name();
	}

	public static HouseType fromValue(String value) {
		return valueOf(value);
	}
}
