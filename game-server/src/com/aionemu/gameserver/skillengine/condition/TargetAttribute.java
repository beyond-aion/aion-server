package com.aionemu.gameserver.skillengine.condition;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "TargetAttribute")
@XmlEnum
public enum TargetAttribute {
	NPC,
	PC,
	ALL,
	SELF,
	NONE;

	public String value() {
		return name();
	}

	public static TargetAttribute fromValue(String v) {
		return valueOf(v);
	}

}
