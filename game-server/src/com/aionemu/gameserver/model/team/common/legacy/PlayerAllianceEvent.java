package com.aionemu.gameserver.model.team.common.legacy;

/**
 * @author Sarynth
 */
public enum PlayerAllianceEvent {
	LEAVE(0),
	BANNED(0),

	MOVEMENT(1),

	DISCONNECTED(3),

	JOIN(5),
	ENTER_OFFLINE(7),

	// Similar to 0, 1, 3 -- only the initial information block.
	UPDATE_EFFECTS(65),

	RECONNECT(13),
	ENTER(13),
	UPDATE(13),

	MEMBER_GROUP_CHANGE(5),

	// Extra? Unused?
	APPOINT_VICE_CAPTAIN(13),
	DEMOTE_VICE_CAPTAIN(13),
	APPOINT_CAPTAIN(13);

	private int id;

	private PlayerAllianceEvent(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}
}
