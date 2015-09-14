package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
