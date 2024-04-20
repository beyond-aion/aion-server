package com.aionemu.gameserver.model.gameobjects.player;

import java.util.LinkedHashMap;
import java.util.Map;

import com.aionemu.gameserver.model.trade.TradePSItem;

/**
 * @author Xav, Simple
 */
public class PrivateStore {

	private final Player owner;
	private Map<Integer, TradePSItem> items;
	private String storeMessage;

	public PrivateStore(Player owner) {
		this.owner = owner;
		this.items = new LinkedHashMap<>();
	}

	public Player getOwner() {
		return owner;
	}

	public Map<Integer, TradePSItem> getSoldItems() {
		return items;
	}

	public void addItemToSell(int itemObjId, TradePSItem tradeItem) {
		items.put(itemObjId, tradeItem);
	}

	public void removeItem(int itemObjId) {
		if (items.containsKey(itemObjId)) {
			Map<Integer, TradePSItem> newItems = new LinkedHashMap<>();
			for (int itemObjIds : items.keySet()) {
				if (itemObjId != itemObjIds)
					newItems.put(itemObjIds, items.get(itemObjIds));
			}
			this.items = newItems;
		}
	}

	public TradePSItem getTradeItemByObjId(int itemObjId) {
		return items.get(itemObjId);
	}

	public void setStoreMessage(String storeMessage) {
		this.storeMessage = storeMessage;
	}

	public String getStoreMessage() {
		return storeMessage == null ? "" : storeMessage;
	}
}
