package com.aionemu.gameserver.model;

/**
 * @author ATracer
 */
public enum TaskId {
	DECAY,
	RESPAWN,
	PRISON,
	PROTECTION_ACTIVE,
	DROWN,
	DESPAWN,
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
