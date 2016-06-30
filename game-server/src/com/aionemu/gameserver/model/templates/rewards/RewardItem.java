package com.aionemu.gameserver.model.templates.rewards;

/**
 * @author Neon
 */
public class RewardItem {

	private final int id;
	private final long count;

	public RewardItem(int id, long count) {
		this.id = id;
		this.count = count;
	}

	public int getId() {
		return id;
	}

	public long getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "RewardItem [id=" + id + ", count=" + count + "]";
	}
}
