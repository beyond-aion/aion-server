package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "ProvokeTarget")
@XmlEnum
public enum ProvokeTarget {
	ME,
	OPPONENT
}
