package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Sippolo
 */
@XmlType(name = "attackType")
@XmlEnum
public enum AttackType {
	EVERYHIT,
	PHYSICAL_SKILL,
	MAGICAL_SKILL,
	ALL_SKILL
}
