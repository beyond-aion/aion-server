package com.aionemu.gameserver.skillengine.properties;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "FirstTargetAttribute")
@XmlEnum
public enum FirstTargetAttribute {
	TARGETORME,
	ME,
	MYPET,
	MYMASTER,
	TARGET,
	PASSIVE,
	TARGET_MYPARTY_NONVISIBLE,
	POINT
}
