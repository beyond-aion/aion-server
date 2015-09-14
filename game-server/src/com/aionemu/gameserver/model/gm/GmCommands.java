package com.aionemu.gameserver.model.gm;

/**
 * @author xTz, Rolandas
 */
public enum GmCommands {

	GM_MAIL_LIST,
	INVENTORY,
	SKILL,
	TELEPORTTO,
	STATUS,
	SEARCH,
	QUEST,
	GM_GUILDHISTORY,
	GM_BUDDY_LIST,
	RECALL,
	GM_COMMENT_LSIT,
	GM_COMMENT_ADD,
	CHECK_BOT1,
	CHECK_BOT99,
	BOOKMARK_ADD,
	GUILD,
	// for dev_builder dialog
	PARTYRECALL,
	RESURRECT,
	CLEARUSERCOOLT,
	ATTRBONUS,
	ADDSKILL,
	WISHID,
	LEVELUP,
	SET_VITALPOINT,
	SET_MAKEUP_BONUS,
	SETINVENTORYGROWTH,
	SKILLPOINT,
	COMBINESKILL,
	TELEPORT_TO_NAMED;

	public static GmCommands getValue(String command) {
		for (GmCommands value : values()) {
			if (value.name().equals(command.toUpperCase())) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid GmCommands id: " + command);
	}
}
