package com.aionemu.gameserver.model.templates.rewards;

/**
 * @author KID, Neon
 */
public class RewardEntryItem extends RewardItem {

	private final int entryId;

	public RewardEntryItem(int entryId, int itemId, long count) {
		super(itemId, count);
		this.entryId = entryId;
	}

	public int getEntryId() {
		return entryId;
	}
}
