package com.aionemu.gameserver.skillengine.properties;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
