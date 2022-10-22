package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yeats
 */
@XmlType(name = "ConditionType")
@XmlEnum
public enum NpcSkillCondition {

	NONE,
	SELECT_RANDOM_ENEMY,
	SELECT_RANDOM_ENEMY_EXCEPT_MOST_HATED,
	SELECT_TARGET_AFFECTED_BY_SKILL,
	HELP_FRIEND,
	TARGET_IS_IN_ANY_STUN,
	TARGET_IS_IN_RANGE,
	TARGET_IS_IN_STUMBLE,
	TARGET_IS_STUNNED,
	TARGET_IS_SLEEPING,
	TARGET_IS_AETHERS_HOLD,
	TARGET_IS_POISONED,
	TARGET_IS_BLEEDING,
	TARGET_IS_FLYING,
	TARGET_IS_GATE,
	TARGET_IS_PLAYER,
	TARGET_IS_NPC,
	TARGET_IS_PHYSICAL_CLASS,
	TARGET_IS_MAGICAL_CLASS,
	TARGET_HAS_CARVED_SIGNET,
	TARGET_HAS_CARVED_SIGNET_LEVEL_II,
	TARGET_HAS_CARVED_SIGNET_LEVEL_III,
	TARGET_HAS_CARVED_SIGNET_LEVEL_IV,
	TARGET_HAS_CARVED_SIGNET_LEVEL_V,
	NPC_IS_ALIVE;

	public String value() {
		return name();
	}

	public static NpcSkillCondition fromValue(String s) {
		return valueOf(s);
	}
}
