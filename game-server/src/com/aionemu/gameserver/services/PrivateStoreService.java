package com.aionemu.gameserver.services;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PrivateStore;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.trade.TradeItem;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.model.trade.TradePSItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PRIVATE_STORE_NAME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

import javolution.util.FastTable;

/**
 * @author Simple
 */
public class PrivateStoreService {

	private static final Logger log = LoggerFactory.getLogger("EXCHANGE_LOG");

	/**
	 * @param activePlayer
	 * @param itemObjId
	 * @param itemId
	 * @param itemAmount
	 * @param itemPrice
	 */
	public static void addItems(Player activePlayer, TradePSItem[] tradePSItems) {
		if (CreatureState.ACTIVE.getId() != activePlayer.getState())
			return;

		if (activePlayer.getStore() == null) // TODO synchronization
			createStore(activePlayer);

		PrivateStore store = activePlayer.getStore();
		for (int i = 0; i < tradePSItems.length; i++) {
			Item item = activePlayer.getInventory().getItemByObjId(tradePSItems[i].getItemObjId());
			if (!validateItem(store, item, tradePSItems[i]))
				break;
			store.addItemToSell(tradePSItems[i].getItemObjId(), tradePSItems[i]);
		}
	}

	private static final boolean validateItem(PrivateStore store, Item item, TradePSItem psItem) {
		if (item == null || psItem.getItemId() != item.getItemTemplate().getTemplateId()) {
			return false;
		}
		if (psItem.getCount() > item.getItemCount() || psItem.getCount() < 1) {
			return false;
		}
		if (psItem.getPrice() < 0) {
			return false;
		}
		if (store.getSoldItems().size() == 10) {
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_FULL_BASKET);
			return false;
		}
		if (item.getPackCount() <= 0 && !item.isTradeable(store.getOwner())) {
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_CANNOT_BE_EXCHANGED);
			return false;
		}
		if (item.isEquipped()) {
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_CAN_NOT_SELL_EQUIPED_ITEM);
			return false;
		}
		if (store.getTradeItemByObjId(psItem.getItemObjId()) != null) {
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_ALREAY_REGIST_ITEM);
			return false;
		}
		return true;
	}

	/**
	 * This method will create the player's store
	 * 
	 * @param activePlayer
	 */
	private static void createStore(Player activePlayer) {
		activePlayer.setStore(new PrivateStore(activePlayer));
		activePlayer.setState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_EMOTION(activePlayer, EmotionType.OPEN_PRIVATESHOP, 0, 0), true);
	}

	/**
	 * This method will destroy the player's store
	 * 
	 * @param activePlayer
	 */
	public static void closePrivateStore(Player activePlayer) {
		activePlayer.setStore(null);
		activePlayer.unsetState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_EMOTION(activePlayer, EmotionType.CLOSE_PRIVATESHOP, 0, 0), true);
	}

	/**
	 * This method will move the item to the new player and move kinah to item owner
	 */
	public static void sellStoreItem(Player seller, Player buyer, TradeList tradeList) {
		if (!seller.isOnline() || !buyer.isOnline() || seller.getRace() != buyer.getRace())
			return;

		List<TradePSItem> boughtItems = getBoughtItems(seller, tradeList);
		if (boughtItems == null || boughtItems.isEmpty())
			return; // Invalid items found or store was empty

		if (buyer.getInventory().getFreeSlots() < boughtItems.size())
			return; // TODO message

		long price = 0;
		for (TradePSItem boughtItem : boughtItems)
			price += boughtItem.getPrice() * boughtItem.getCount();

		if (price < 0) { // Kinah dupe
			AuditLogger.info(buyer, "[Private Store] Tried to buy item with negative kinah price.");
			return;
		}

		if (price > buyer.getInventory().getKinah())
			return; // TODO message

		for (TradePSItem boughtItem : boughtItems) {
			Item item = seller.getInventory().getItemByObjId(boughtItem.getItemObjId());
			if (item != null) {
				// Fix "Private store stackable items dupe" by Asanka
				if (item.getItemCount() < boughtItem.getCount()) {
					AuditLogger.info(buyer, "[Private Store] Tried to buy more than player has in stack.");
					return;
				}

				decreaseItemFromPlayer(seller, item, boughtItem);
				// unpack
				if (item.getPackCount() > 0)
					item.setPackCount(item.getPackCount() - 1);

				ItemService.addItem(buyer, item, boughtItem.getCount());

				log.info("[PRIVATE STORE] > [Seller: " + seller.getName() + "] sold [Item: " + item.getItemId() + "][Amount: " + boughtItem.getCount()
					+ "] to [Buyer: " + buyer.getName() + "] for [Price: " + boughtItem.getPrice() * boughtItem.getCount() + "]");
			}
		}
		buyer.getInventory().decreaseKinah(price);
		seller.getInventory().increaseKinah(price);

		if (seller.getStore().getSoldItems().isEmpty())
			closePrivateStore(seller);
	}

	/**
	 * Decrease item count and update inventory
	 * 
	 * @param seller
	 * @param item
	 */
	private static void decreaseItemFromPlayer(Player seller, Item item, TradePSItem boughtItem) {
		seller.getInventory().decreaseItemCount(item, boughtItem.getCount());
		TradePSItem storeItem = seller.getStore().getTradeItemByObjId(item.getObjectId());
		storeItem.decreaseCount(boughtItem.getCount());
		if (storeItem.getCount() == 0)
			seller.getStore().removeItem(item.getObjectId());
	}

	/**
	 * @param seller
	 * @param tradeList
	 * @return
	 */
	private static List<TradePSItem> getBoughtItems(Player seller, TradeList tradeList) {
		Collection<TradePSItem> storeList = seller.getStore().getSoldItems().values();
		// we need index based access since tradeList holds index values (this will work since underlying LinkedHashMap preserves insertion order)
		TradePSItem[] storeItems = storeList.toArray(new TradePSItem[storeList.size()]);
		List<TradePSItem> boughtItems = new FastTable<>();

		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			if (tradeItem.getItemId() >= 0 && tradeItem.getItemId() < storeItems.length) { // itemId is index! blame the one who implemented this
				TradePSItem storeItem = storeItems[tradeItem.getItemId()];
				if (tradeItem.getCount() > storeItem.getCount()) {
					log.warn("[Private Store] Attempt to buy more than for sale: " + tradeItem.getCount() + " vs. " + storeItem.getCount());
					return null;
				}
				boughtItems.add(new TradePSItem(storeItem.getItemObjId(), storeItem.getItemId(), tradeItem.getCount(), storeItem.getPrice()));
			} else {
				log.warn("[Private Store] Attempt to buy from invalid store index: " + tradeItem.getItemId());
				return null;
			}
		}

		return boughtItems;
	}

	/**
	 * @param activePlayer
	 */
	public static void openPrivateStore(Player activePlayer, String name) {
		activePlayer.getStore().setStoreMessage(name);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_PRIVATE_STORE_NAME(activePlayer), true);
	}
}
