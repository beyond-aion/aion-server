package com.aionemu.gameserver.model.templates.item.bonuses;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "StatBonusType")
@XmlEnum
public enum StatBonusType {

	INVENTORY,
	POLISH;

	public String value() {
		return name();
	}

	public static StatBonusType fromValue(String v) {
		return valueOf(v);
	}

}
