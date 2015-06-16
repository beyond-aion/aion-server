package com.aionemu.gameserver.skillengine.properties;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author kecimis
 *
 */
@XmlType(name = "TargetSpeciesAttribute")
@XmlEnum
public enum TargetSpeciesAttribute {
	NONE,
	ALL,
	PC,
	NPC;

	public String value() {
		return name();
	}

	public static TargetSpeciesAttribute fromValue(String v) {
		return valueOf(v);
	}
}

