package com.aionemu.gameserver.skillengine.change;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
