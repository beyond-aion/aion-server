package com.aionemu.gameserver.model.templates.challenge;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Rates")
@XmlEnum
public enum RewardType {
	NONE,
	POINT,
	SPAWN;

	public String value() {
		return name();
	}

	public static RewardType fromValue(String paramString) {
		return valueOf(paramString);
	}
}
