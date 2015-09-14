package com.aionemu.gameserver.model.trade;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class TradeItem {

	private int itemId;
	private long count;
	private ItemTemplate itemTemplate;

	public TradeItem(int itemId, long count) {
		super();
		this.itemId = itemId;
		this.count = count;
	}

	/**
	 * @return the itemTemplate
	 */
	public ItemTemplate getItemTemplate() {
		return itemTemplate;
	}

	/**
	 * @param itemTemplate
	 *          the itemTemplate to set
	 */
	public void setItemTemplate(ItemTemplate itemTemplate) {
		this.itemTemplate = itemTemplate;
	}

	/**
	 * @return the itemId
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * This method will decrease the current count
	 */
	public void decreaseCount(long decreaseCount) {
		// TODO probably <= count ?
		if (decreaseCount < count)
			this.count = count - decreaseCount;
	}
}
