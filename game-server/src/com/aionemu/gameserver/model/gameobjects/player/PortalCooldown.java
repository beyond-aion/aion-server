package com.aionemu.gameserver.model.gameobjects.player;

/**
 * @author ViAl
 */
public class PortalCooldown {

	private int worldId;
	private long reuseTime;
	private int enterCount;

	public PortalCooldown(int worldId, long reuseTime, int enterCount) {
		this.worldId = worldId;
		this.reuseTime = reuseTime;
		this.enterCount = enterCount;
	}

	public void increaseEnterCount() {
		this.enterCount++;
	}

	public void decreaseEnterCount(int count) {
		this.enterCount -= count;
	}

	public int getWorldId() {
		return worldId;
	}

	public long getReuseTime() {
		return reuseTime;
	}

	public int getEnterCount() {
		return enterCount;
	}
}
