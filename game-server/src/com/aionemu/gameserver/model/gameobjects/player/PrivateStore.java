package com.aionemu.gameserver.model.gameobjects.player;

import java.util.LinkedHashMap;
import java.util.Map;

import com.aionemu.gameserver.model.trade.TradePSItem;

/**
 * @author Xav
 * @modified Simple
 */
public class PrivateStore {

	private final Player owner;
	private Map<Integer, TradePSItem> items;
	private String storeMessage;

	/**
	 * This method binds a player to the store and creates a list of items
	 * 
	 * @param owner
	 */
	public PrivateStore(Player owner) {
		this.owner = owner;
		this.items = new LinkedHashMap<>();
	}

	/**
	 * This method will return the owner of the store
	 * 
	 * @return Player
	 */
	public Player getOwner() {
		return owner;
	}

	/**
	 * This method will return the items being sold
	 * 
	 * @return Map<Integer, TradePSItem>
	 */
	public Map<Integer, TradePSItem> getSoldItems() {
		return items;
	}

	/**
	 * This method will add an item to the list and price
	 * 
	 * @param tradeList
	 * @param price
	 */
	public void addItemToSell(int itemObjId, TradePSItem tradeItem) {
		items.put(itemObjId, tradeItem);
	}

	/**
	 * This method will remove an item from the list
	 * 
	 * @param item
	 */
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

	/**
	 * @param itemId
	 *          return tradeItem
	 */
	public TradePSItem getTradeItemByObjId(int itemObjId) {
		return items.get(itemObjId);
	}

	/**
	 * @param storeMessage
	 *          the storeMessage to set
	 */
	public void setStoreMessage(String storeMessage) {
		this.storeMessage = storeMessage;
	}

	/**
	 * @return the storeMessage
	 */
	public String getStoreMessage() {
		return storeMessage == null ? "" : storeMessage;
	}
}
