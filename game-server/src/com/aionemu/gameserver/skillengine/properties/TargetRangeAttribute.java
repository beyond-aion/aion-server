package com.aionemu.gameserver.skillengine.properties;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
