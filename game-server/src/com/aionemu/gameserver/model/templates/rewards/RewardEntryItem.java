package com.aionemu.gameserver.model.templates.rewards;

/**
 * @author KID
 */
public class RewardEntryItem
{
	public RewardEntryItem(int unique, int item_id, long count) {
		this.unique = unique;
		this.id = item_id;
		this.count = count;
	}
	public int id, unique;
	public long count;
}