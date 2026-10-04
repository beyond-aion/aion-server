package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "HopType")
@XmlEnum
public enum HopType {
	DAMAGE,
	SKILLLV
}
