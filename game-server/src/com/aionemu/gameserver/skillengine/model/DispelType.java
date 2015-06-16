package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "DispelType")
@XmlEnum
public enum DispelType {

	EFFECTID,
	EFFECTIDRANGE,
	EFFECTTYPE,
	SLOTTYPE;

	public String value() {
		return name();
	}

	public static DispelType fromValue(String v) {
		return valueOf(v);
	}

}
