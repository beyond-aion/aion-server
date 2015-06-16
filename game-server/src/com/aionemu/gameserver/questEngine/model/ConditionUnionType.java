package com.aionemu.gameserver.questEngine.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author Mr. Poke
 */
@XmlEnum
public enum ConditionUnionType {

	AND,
	OR;

	public String value() {
		return name();
	}

	public static ConditionUnionType fromValue(String v) {
		return valueOf(v);
	}

}
