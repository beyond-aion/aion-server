package com.aionemu.gameserver.model.templates.npcshout;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */

@XmlType(name = "ShoutEventType")
@XmlEnum
public enum ShoutEventType {

	IDLE,
	ATTACKED, // NPC was being attacked (the same as aggro)
	ATTACK_BEGIN, // NPC starts an attack (the same as aggro)
	ATTACK_END, // NPC leaves FIGHT state
	ATTACK_K, // Numeric hit shouts
	SUMMON_ATTACK, // Summon attack
	CASTING,
	CAST_K, // Numeric cast shouts
	DIED, // Npc died
	HELP, // Calls help without running away
	HELPCALL, // Calls help and runs away
	WALK_WAYPOINT, // Reached the walk point
	START,
	WAKEUP,
	SLEEP,
	RESET_HATE,
	UNK_ACC, // Not clear but seems the same as ATTACKED, probably related to skill acc_mod
	WALK_DIRECTION, // NPC reached the 0 walk point
	STATUP, // Skill statup shouts
	SWITCH_TARGET, // NPC switched the target
	SEE, // NPC sees a player from aggro range
	PLAYER_MAGIC, // Player uses magic attack (merge with attacked?)
	PLAYER_SNARE,
	PLAYER_DEBUFF,
	PLAYER_SKILL,
	PLAYER_SLAVE,
	PLAYER_BLOW,
	PLAYER_PULL,
	PLAYER_PROVOKE,
	PLAYER_CAST,
	GOD_HELP,
	LEAVE, // when player leaves an attack
	BEFORE_DESPAWN, // NPC despawns
	ATTACK_DEADLY,
	WIN,
	ENEMY_DIED, // NPC's enemy died
	ENTER_BATTLE,
	LEAVE_BATTLE,
	DEFORM_SKILL,
	ATTACK_HITPOINT;

	public String value() {
		return name();
	}

	public static ShoutEventType fromValue(String v) {
		return valueOf(v);
	}

}
