package com.aionemu.gameserver.model;

/**
 * @author Luno modified by Wakizashi (CHEST)
 */
public enum CreatureType {
	NULL(-1),
	/** These are regular monsters */
	ATTACKABLE(0),
	/** These are Peace npc */
	PEACE(2),
	/** These are monsters that are pre-aggressive */
	AGGRESSIVE(8),
	// unk
	INVULNERABLE(10),
	/** These are non attackable NPCs */
	FRIEND(38),

	SUPPORT(54);

	private int someClientSideId;

	private CreatureType(int id) {
		this.someClientSideId = id;
	}

	public int getId() {
		return someClientSideId;
	}

	public static CreatureType getCreatureType(int id) {
		for (CreatureType ct : values()) {
			if (ct.getId() == id)
				return ct;
		}
		return null;
	}
}
