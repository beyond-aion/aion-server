package com.aionemu.gameserver.model.templates.materials;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "DayTime")
@XmlEnum
public enum MaterialActTime {

	DAY,
	NIGHT;

	public String value() {
		return name();
	}

	public static MaterialActTime fromValue(String value) {
		return valueOf(value);
	}

}
