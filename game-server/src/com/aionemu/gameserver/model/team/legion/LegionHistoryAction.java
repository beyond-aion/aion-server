package com.aionemu.gameserver.model.team.legion;

/**
 * @author Simple
 */
public enum LegionHistoryAction {

	CREATE(0, Type.LEGION), // No parameters
	JOIN(1, Type.LEGION), // Parameter: name
	KICK(2, Type.LEGION), // Parameter: name
	LEVEL_UP(3, Type.LEGION), // Parameter: legion level
	APPOINTED(4, Type.LEGION), // Parameter: legion level
	EMBLEM_REGISTER(5, Type.LEGION), // No parameters
	EMBLEM_MODIFIED(6, Type.LEGION), // No parameters
	// 7 to 10 are not used anymore or never implemented
	DEFENSE(11, Type.REWARD), // Parameter: name = kinah amount, description = fortress id
	OCCUPATION(12, Type.REWARD), // Parameter: name = kinah amount, description = fortress id
	LEGION_RENAME(13, Type.LEGION), // Parameter: old name, new name
	CHARACTER_RENAME(14, Type.LEGION), // Parameter: old name, new name
	ITEM_DEPOSIT(15, Type.WAREHOUSE), // Parameter: name
	ITEM_WITHDRAW(16, Type.WAREHOUSE), // Parameter: name
	KINAH_DEPOSIT(17, Type.WAREHOUSE), // Parameter: name
	KINAH_WITHDRAW(18, Type.WAREHOUSE); // Parameter: name

	private final byte id;
	private final Type type;

	LegionHistoryAction(int id, Type type) {
		this.id = (byte) id;
		this.type = type;
	}

	public byte getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public enum Type { LEGION, REWARD, WAREHOUSE }
}
