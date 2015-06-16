package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
