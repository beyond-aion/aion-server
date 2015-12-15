package com.aionemu.gameserver.model.trade;

import com.aionemu.gameserver.model.gameobjects.Item;

/**
 * @author ATracer
 */
public class ExchangeItem {

	private int itemObjId;
	private long itemCount;
	private Item item;

	/**
	 * Used when exchange item != original item
	 * 
	 * @param itemObjId
	 * @param itemCount
	 * @param item
	 */
	public ExchangeItem(int itemObjId, long itemCount, Item item) {
		this.itemObjId = itemObjId;
		this.itemCount = itemCount;
		this.item = item;
	}

	/**
	 * @param item
	 *          the item to set
	 */
	public void setItem(Item item) {
		this.item = item;
	}

	/**
	 * @param countToAdd
	 */
	public void addCount(long countToAdd) {
		this.itemCount += countToAdd;
		this.item.setItemCount(itemCount);
	}

	/**
	 * @return the newItem
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * @return the itemObjId
	 */
	public int getItemObjId() {
		return itemObjId;
	}

	/**
	 * @return the itemCount
	 */
	public long getItemCount() {
		return itemCount;
	}
}
