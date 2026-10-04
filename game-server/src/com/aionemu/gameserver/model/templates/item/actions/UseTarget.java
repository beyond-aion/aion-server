package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "UseTarget")
@XmlEnum
public enum UseTarget {

	ACCESSORY,
	ARMOR,
	EQUIPMENT,
	WEAPON,
	WING,
	OTHER,
	ALL;

	public String value() {
		return name();
	}

	public static UseTarget fromValue(String v) {
		return valueOf(v);
	}

}
