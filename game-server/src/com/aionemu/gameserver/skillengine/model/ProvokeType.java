package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "ProvokeType")
@XmlEnum
public enum ProvokeType {
	ATTACK,
	ATTACKED
}
