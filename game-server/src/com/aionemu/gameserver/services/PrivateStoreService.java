package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
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
import com.aionemu.gameserver.services.RecallService.CancelReason;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Simple
 */
public class PrivateStoreService {

	private static final Logger log = LoggerFactory.getLogger("EXCHANGE_LOG");

	public static void createStoreWithItems(Player player, TradePSItem[] tradePSItems) {
		if (!canOpenPrivateStore(player))
			return;

		PrivateStore store = new PrivateStore(player);
		for (TradePSItem tradePSItem : tradePSItems) {
			Item item = player.getInventory().getItemByObjId(tradePSItem.getItemObjId());
			if (!validateItem(store, item, tradePSItem))
				return;
			store.addItemToSell(tradePSItem.getItemObjId(), tradePSItem);
		}
		player.setStore(store);
		player.setState(CreatureState.PRIVATE_SHOP, true);
		RecallService.getInstance().cancel(player, CancelReason.CANCELLED);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.OPEN_PRIVATESHOP, 0, 0), true);
	}

	private static boolean canOpenPrivateStore(Player player) {
		if (player.isFlying()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_FLY_MODE());
			return false;
		}
		if (player.getMoveController().isInMove()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_MOVING_OBJECT());
			return false;
		}
		if (player.isInAttackMode()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_COMBAT_MODE());
			return false;
		}
		if (player.isTrading()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_OPEN_STORE_DURING_CRAFTING()); // name "crafting" is NC fail, msg is correct
			return false;
		}
		if (player.isInPlayerMode(PlayerMode.RIDE) || player.isInRobotMode()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PERSONAL_SHOP_RESTRICTION_RIDE());
			return false;
		}
		if (player.getEffectController().isAbnormalSet(AbnormalState.HIDE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_HIDDEN_MODE());
			return false;
		}
		if (player.isDead())
			return false;
		if (player.isInState(CreatureState.CHAIR))
			return false;
		if (player.getStore() != null)
			return false;
		return true;
	}

	private static boolean validateItem(PrivateStore store, Item item, TradePSItem psItem) {
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
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_FULL_BASKET());
			return false;
		}
		if (item.getPackCount() <= 0 && !item.isTradeable()) {
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_CANNOT_BE_EXCHANGED());
			return false;
		}
		if (item.isEquipped()) {
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_CAN_NOT_SELL_EQUIPED_ITEM());
			return false;
		}
		if (store.getTradeItemByObjId(psItem.getItemObjId()) != null) {
			PacketSendUtility.sendPacket(store.getOwner(), SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_ALREAY_REGIST_ITEM());
			return false;
		}
		return true;
	}

	public static void closePrivateStore(Player player) {
		if (player.getStore() == null)
			return;
		player.setStore(null);
		player.unsetState(CreatureState.PRIVATE_SHOP);
		player.setState(CreatureState.ACTIVE);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.CLOSE_PRIVATESHOP, 0, 0), true);
	}

	/**
	 * This method will move the item to the new player and move kinah to item owner
	 */
	public static void sellStoreItem(Player seller, Player buyer, TradeList tradeList) {
		if (!seller.isOnline() || !buyer.isOnline() || seller.getRace() != buyer.getRace())
			return;

		PrivateStore store = seller.getStore();
		if (store == null)
			return;

		long refund = 0;
		synchronized (store) { // buyers must not take the same stack twice: the store count is the only limit
			List<TradePSItem> boughtItems = getBoughtItems(seller, store, tradeList);
			if (boughtItems == null || boughtItems.isEmpty())
				return; // Invalid items found or store was empty

			if (buyer.getInventory().getFreeSlots() < boughtItems.size()) {
				PacketSendUtility.sendPacket(buyer, SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR());
				return;
			}

			long price = 0;
			try {
				for (TradePSItem boughtItem : boughtItems)
					price = Math.addExact(price, Math.multiplyExact(boughtItem.getPrice(), boughtItem.getCount()));
			} catch (ArithmeticException e) {
				AuditLogger.log(buyer, "tried to buy items for more than " + Long.MAX_VALUE + " kinah from private store of " + seller);
				return;
			}

			if (price < 0) { // Kinah dupe
				AuditLogger.log(buyer, "tried to buy item with negative kinah price from private store");
				return;
			}

			for (TradePSItem boughtItem : boughtItems) { // the stack may be gone since the snapshot
				Item item = seller.getInventory().getItemByObjId(boughtItem.getItemObjId());
				if (item == null || item.getItemCount() < boughtItem.getCount())
					return;
			}

			if (!buyer.getInventory().tryDecreaseKinah(price))
				return;

			for (TradePSItem boughtItem : boughtItems) {
				Item item = seller.getInventory().getItemByObjId(boughtItem.getItemObjId());
				long soldCount = item == null ? 0 : decreaseItemFromPlayer(seller, store, item, boughtItem);
				if (soldCount < boughtItem.getCount())
					refund += boughtItem.getPrice() * (boughtItem.getCount() - soldCount);
				if (soldCount == 0)
					continue;
				// unpack
				if (item.getPackCount() > 0)
					item.setPackCount(item.getPackCount() - 1);

				ItemService.addItem(buyer, item, soldCount);

				if (soldCount == 1)
					PacketSendUtility.sendPacket(seller, SM_SYSTEM_MESSAGE.STR_MSG_PERSONAL_SHOP_SELL_ITEM(item.getL10n()));
				else
					PacketSendUtility.sendPacket(seller, SM_SYSTEM_MESSAGE.STR_MSG_PERSONAL_SHOP_SELL_ITEM_MULTI(soldCount, item.getL10n()));
				log.info("[PRIVATE STORE] > [Seller: {}] sold [Item: {}][Amount: {}] to [Buyer: {}] for [Price: {}]", seller.getName(),
					item.getItemId(), soldCount, buyer.getName(), boughtItem.getPrice() * soldCount);
			}
			if (refund > 0)
				buyer.getInventory().increaseKinah(refund);
			seller.getInventory().increaseKinah(price - refund);
		}

		if (store.getSoldItems().isEmpty())
			closePrivateStore(seller);
	}

	/**
	 * Decrease item count and update inventory
	 * 
	 * @return the removed amount, which may be less than requested
	 */
	private static long decreaseItemFromPlayer(Player seller, PrivateStore store, Item item, TradePSItem boughtItem) {
		long soldCount = boughtItem.getCount() - seller.getInventory().decreaseItemCount(item, boughtItem.getCount());
		if (soldCount <= 0)
			return 0;
		TradePSItem storeItem = store.getTradeItemByObjId(item.getObjectId());
		storeItem.decreaseCount(soldCount);
		if (storeItem.getCount() == 0)
			store.removeItem(item.getObjectId());
		return soldCount;
	}

	/**
	 * @param tradeList
	 *          entries with the seller's store indices, each index at most once
	 */
	private static List<TradePSItem> getBoughtItems(Player seller, PrivateStore store, TradeList tradeList) {
		Collection<TradePSItem> storeList = store.getSoldItems().values();
		// we need index based access since tradeList holds index values (this will work since underlying LinkedHashMap preserves insertion order)
		TradePSItem[] storeItems = storeList.toArray(new TradePSItem[storeList.size()]);
		List<TradePSItem> boughtItems = new ArrayList<>();
		for (TradeItem tradeItem : tradeList.getTradeItems()) {
			int index = tradeItem.getItemId(); // itemId is index! blame the one who implemented this
			if (index < 0 || index >= storeItems.length) {
				log.warn("[Private Store] Attempt to buy item with invalid index: {}", index);
				return null;
			}
			TradePSItem storeItem = storeItems[index];
			long count = tradeItem.getCount();
			if (count > storeItem.getCount()) {
				log.warn("[Private Store] Attempt to buy more than for sale: {} vs. {}", count, storeItem.getCount());
				return null;
			}
			Item item = seller.getInventory().getItemByObjId(storeItem.getItemObjId());
			if (item == null || item.getItemCount() < count)
				return null;
			boughtItems.add(new TradePSItem(storeItem.getItemObjId(), storeItem.getItemId(), count, storeItem.getPrice()));
		}
		return boughtItems;
	}

	public static void openPrivateStore(Player activePlayer, String name) {
		if (name != null && name.length() > PrivateStore.MAX_STORE_MESSAGE_LENGTH) // retail refuses longer messages instead of truncating them
			return;
		activePlayer.getStore().setStoreMessage(name);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_PRIVATE_STORE_NAME(activePlayer), true);
	}
}
