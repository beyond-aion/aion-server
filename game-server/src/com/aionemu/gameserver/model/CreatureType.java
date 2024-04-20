package com.aionemu.gameserver.model;

/**
 * @author Luno, Wakizashi
 */
public enum CreatureType {

	/** These are regular monsters */
	ATTACKABLE(0),
	/** These are Peace npc, which you cannot talk to */
	PEACE(2),
	/** These are monsters that are pre-aggressive */
	AGGRESSIVE(8),
	// unk
	INVULNERABLE(10),
	/** These are non attackable NPCs, which you can talk to */
	FRIEND(38),

	SUPPORT(54);

	private int id;

	CreatureType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
