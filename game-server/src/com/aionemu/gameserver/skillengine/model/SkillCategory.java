package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Cheatkiller
 */
@XmlType(name = "skillCategory")
@XmlEnum
public enum SkillCategory {
	NONE,
	CHAIN_SKILL,
	PHYSICAL_DEBUFF,
	HEAL,
	MENTAL_DEBUFF,
	REBIRTH,
	DISPELL,
	DEATHBLOW,
	DRAIN
}
