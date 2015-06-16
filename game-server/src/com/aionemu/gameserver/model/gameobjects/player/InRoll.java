package com.aionemu.gameserver.model.gameobjects.player;

/**
 * @author xTz
 */
public class InRoll {

	private int npcId;
	private int itemId;
	private int rollType;
	private int index;

	public InRoll(int npcId, int itemId, int index, int rollType) {
		this.npcId = npcId;
		this.itemId = itemId;
		this.index = index;
		this.rollType = rollType;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getItemId() {
		return itemId;
	}

	public int getIndex() {
		return index;
	}

	public int getRollType() {
		return rollType;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public void setIndexd(int index) {
		this.index = itemId;
	}

	public void setRollType(int rollType) {
		this.rollType = rollType;
	}
}
