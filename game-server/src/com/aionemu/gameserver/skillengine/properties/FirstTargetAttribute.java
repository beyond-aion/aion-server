package com.aionemu.gameserver.skillengine.properties;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

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
