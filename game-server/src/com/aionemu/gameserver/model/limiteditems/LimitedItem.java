package com.aionemu.gameserver.model.limiteditems;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xTz, Neon
 */
public class LimitedItem {

	private int itemId;
	private int sellLimit;
	private int buyLimit;
	private int defaultSellLimit;
	private String salesTime;
	private Map<Integer, Integer> buyCounts = new HashMap<>();

	public LimitedItem(int itemId, int sellLimit, int buyLimit, String salesTime) {
		this.itemId = itemId;
		this.sellLimit = sellLimit;
		this.buyLimit = buyLimit;
		this.defaultSellLimit = sellLimit;
		this.salesTime = salesTime;
	}

	public int getItemId() {
		return itemId;
	}

	public void setBuyCount(int playerObjectId, int count) {
		buyCounts.put(playerObjectId, count);
	}

	public int getBuyCount(int playerObjectId) {
		return buyCounts.getOrDefault(playerObjectId, 0);
	}

	public void setItem(int itemId) {
		this.itemId = itemId;
	}

	public int getSellLimit() {
		return sellLimit;
	}

	public int getBuyLimit() {
		return buyLimit;
	}

	public void setToDefault() {
		sellLimit = defaultSellLimit;
		buyCounts.clear();
	}

	public void setSellLimit(int sellLimit) {
		this.sellLimit = sellLimit;
	}

	public int getDefaultSellLimit() {
		return defaultSellLimit;
	}

	public String getSalesTime() {
		return salesTime;
	}
}
