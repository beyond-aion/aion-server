package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "skillSubType")
@XmlEnum
public enum SkillSubType {
	NONE,
	ATTACK,
	CHANT,
	HEAL,
	BUFF,
	DEBUFF,
	SUMMON,
	SUMMONHOMING,
	SUMMONTRAP
}
