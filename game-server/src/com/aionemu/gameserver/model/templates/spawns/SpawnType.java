package com.aionemu.gameserver.model.templates.spawns;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */

@XmlType(name = "SpawnType")
@XmlEnum
public enum SpawnType {

	MANAGER,
	TELEPORT,
	SIGN;

	public String value() {
		return name();
	}

	public static SpawnType fromValue(String v) {
		return valueOf(v);
	}

}
