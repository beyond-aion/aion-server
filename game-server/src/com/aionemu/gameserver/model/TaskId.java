package com.aionemu.gameserver.model;

/**
 * @author ATracer
 */
public enum TaskId {
	DECAY,
	RESPAWN, // npc respawn task / player teleport task after leave animation
	PRISON,
	PROTECTION_ACTIVE,
	DROWN,
	DESPAWN, // npc despawn task / player leaveWorld task after dc/sendlog error
	/**
	 * Quest task with timer
	 */
	QUEST_TIMER,
	/**
	 * Follow task checker
	 */
	QUEST_FOLLOW,
	PLAYER_UPDATE,
	INVENTORY_UPDATE,
	GAG,
	ITEM_USE,
	ACTION_ITEM_NPC,
	HOUSE_OBJECT_USE,
	EXPRESS_MAIL_USE,
	SKILL_USE,
	GATHERABLE,
	PET_UPDATE,
	SUMMON_FOLLOW,
	MATERIAL_ACTION,
	SHOUT
}
