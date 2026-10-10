package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

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
