package com.aionemu.gameserver.model.trade;

/**
 * @author Simple
 * @modified Neon
 */
public class TradePSItem extends TradeItem {

	private int itemObjId;
	private long price;

	/**
	 * @param itemId
	 * @param count
	 */
	public TradePSItem(int itemObjId, int itemId, long count, long price) {
		super(itemId, count);
		this.setPrice(price);
		this.setItemObjId(itemObjId);
	}

	/**
	 * @param price
	 *          the price to set
	 */
	public void setPrice(long price) {
		this.price = price;
	}

	/**
	 * @return the price
	 */
	public long getPrice() {
		return price;
	}

	/**
	 * @param itemObjId
	 *          the itemObjId to set
	 */
	public void setItemObjId(int itemObjId) {
		this.itemObjId = itemObjId;
	}

	/**
	 * @return the itemObjId
	 */
	public int getItemObjId() {
		return itemObjId;
	}

	/**
	 * Decreases the count only if it would really decrease and wouldn't become negative
	 */
	public void decreaseCount(long decreaseCount) {
		if (decreaseCount > 0)
			this.count -= Math.min(decreaseCount, count);
	}
}
