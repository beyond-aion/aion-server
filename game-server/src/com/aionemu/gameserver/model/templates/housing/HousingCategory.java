package com.aionemu.gameserver.model.templates.housing;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "HousingObjectType")
@XmlEnum
public enum HousingCategory {

	BED,
	BOOK,
	CARPET,
	CHAIR,
	CURTAIN,
	DECORATION,
	LIGHT,
	NPC,
	OUTLIGHT,
	TABLE;

	public String value() {
		return name();
	}

	public static HousingCategory fromValue(String value) {
		return valueOf(value);
	}

}
