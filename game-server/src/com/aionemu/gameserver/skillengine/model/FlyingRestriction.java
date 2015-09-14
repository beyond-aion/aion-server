package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
