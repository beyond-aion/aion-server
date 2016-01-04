package com.aionemu.gameserver.model.templates.npc;

/**
 * @author Rolandas
 */
public enum SubDialogType {

	ALL_ALLOWED,
	FORT_CAPTURE, // Allow when fort was captured
	SKILL_ID, // Allow when player skill is present
	ITEM_ID, // Allow when item in the inventory exists
	RETURN, // Arena return ?
	PCBANG, // Allow when some event is active
	PAID_USER, // Allow for players with certain membership
	NEWBIE, // Allow for new players
	ABYSSRANK,
	ABYSSRANKING,
	LEVEL,
	LEVEL_LOW,
	LEVEL_HIGH,
	LEGION_DOMINION_NPC,
	TARGET_LEGION_DOMINION,
	PACK_3,
	PACK_4,
	CASH
}
