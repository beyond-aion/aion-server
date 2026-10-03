package com.aionemu.gameserver.skillengine.properties;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "TargetRangeAttribute")
@XmlEnum
public enum TargetRangeAttribute {

	ONLYONE,
	PARTY,
	AREA,
	PARTY_WITHPET,
	POINT
}
