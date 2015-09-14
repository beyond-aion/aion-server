package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "skillType")
@XmlEnum
public enum SkillType {
	NONE,
	PHYSICAL,
	MAGICAL,
	ALL
}
