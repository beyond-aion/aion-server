package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author kecimis
 */
@XmlType(name = "FlyingRestriction")
@XmlEnum
public enum FlyingRestriction {
	ALL,
	FLY,
	GROUND;
}
