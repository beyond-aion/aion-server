package com.aionemu.gameserver.questEngine.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author Mr. Poke
 */
@XmlEnum
public enum ConditionOperation {

	EQUAL,
	GREATER,
	GREATER_EQUAL,
	LESSER,
	LESSER_EQUAL,
	NOT_EQUAL,
	IN,
	NOT_IN;

	public String value() {
		return name();
	}

	public static ConditionOperation fromValue(String v) {
		return valueOf(v);
	}

}
