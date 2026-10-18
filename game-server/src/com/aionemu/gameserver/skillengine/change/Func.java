package com.aionemu.gameserver.skillengine.change;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "Func")
@XmlEnum
public enum Func {
	ADD,
	PERCENT,
	REPLACE
}
