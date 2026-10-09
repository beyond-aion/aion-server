package com.aionemu.gameserver.model.templates.housing;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "PlaceArea")
@XmlEnum
public enum PlaceArea {

	ALL,
	INTERIOR,
	EXTERIOR;

	public String value() {
		return name();
	}

	public static PlaceArea fromValue(String value) {
		return valueOf(value);
	}

}
