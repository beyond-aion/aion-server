package com.aionemu.gameserver.model.team.common.legacy;

/**
 * @author Lyahim
 */
public enum LootRuleType {

	FREEFORALL(0),
	ROUNDROBIN(1),
	LEADER(2);

	private int id;

	private LootRuleType(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	/**
	 * @return The loot rule with the given id, null if there is none.
	 */
	public static LootRuleType getById(int id) {
		for (LootRuleType type : values())
			if (type.id == id)
				return type;
		return null;
	}
}
