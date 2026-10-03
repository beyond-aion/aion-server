package com.aionemu.gameserver.skillengine.properties;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "TargetRelationAttribute")
@XmlEnum
public enum TargetRelationAttribute {

	NONE,
	ENEMY,
	MYPARTY,
	ALL,
	FRIEND
}
