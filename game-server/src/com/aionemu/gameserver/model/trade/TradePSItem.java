package com.aionemu.gameserver.model.trade;

/**
 * @author Simple, Neon
 */
public class TradePSItem extends TradeItem {

	private int itemObjId;
	private long price;

	public TradePSItem(int itemObjId, int itemId, long count, long price) {
		super(itemId, count);
		this.setPrice(price);
		this.setItemObjId(itemObjId);
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public long getPrice() {
		return price;
	}

	public void setItemObjId(int itemObjId) {
		this.itemObjId = itemObjId;
	}

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
