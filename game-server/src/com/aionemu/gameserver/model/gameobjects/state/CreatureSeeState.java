package com.aionemu.gameserver.model.gameobjects.state;

/**
 * @author Sweetkr
 */
public enum CreatureSeeState {
	NORMAL(0), // Normal
	SEARCH1(1), // See-Through: Hide I
	SEARCH2(2), // See-Through: Hide II
	SEARCH5(5), // npc stealth
	SEARCH10(10), // 3.0 npc stealth
	SEARCH20(20);

	private int id;

	private CreatureSeeState(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
