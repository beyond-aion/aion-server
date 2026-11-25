package com.aionemu.gameserver.model.gameobjects.player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.trade.TradePSItem;

/**
 * @author Xav, Simple
 */
public class PrivateStore {

	public static final int MAX_STORE_MESSAGE_LENGTH = 255;

	private final Player owner;
	private volatile Map<Integer, TradePSItem> items; // replaced on sales, read by the packets of the buyers
	private String storeMessage;
	// The buy packet names the items by their position in the list the buyer was shown, which changes as items sell out
	private final Map<Integer, List<Integer>> shownItemObjIds = new ConcurrentHashMap<>();

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

	/**
	 * Remembers the items the buyer was shown, in their order.
	 */
	public void setShownItems(Player buyer, List<Integer> itemObjIds) {
		shownItemObjIds.put(buyer.getObjectId(), List.copyOf(itemObjIds));
	}

	/**
	 * @return The object id of the item at the given position of the list the buyer was shown, 0 if there's none.
	 */
	public int getShownItemObjId(Player buyer, int index) {
		List<Integer> itemObjIds = shownItemObjIds.get(buyer.getObjectId());
		return itemObjIds == null || index < 0 || index >= itemObjIds.size() ? 0 : itemObjIds.get(index);
	}

	public TradePSItem getTradeItemByObjId(int itemObjId) {
		return items.get(itemObjId);
	}

	public void setStoreMessage(String storeMessage) {
		// Part of SM_PLAYER_INFO and SM_PRIVATE_STORE, which must stay below the client's 8192 byte limit.
		this.storeMessage = storeMessage == null || storeMessage.length() <= MAX_STORE_MESSAGE_LENGTH ? storeMessage
			: storeMessage.substring(0, MAX_STORE_MESSAGE_LENGTH);
	}

	public String getStoreMessage() {
		return storeMessage == null ? "" : storeMessage;
	}
}
