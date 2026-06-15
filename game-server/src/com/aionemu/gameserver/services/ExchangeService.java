package com.aionemu.gameserver.services;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.trade.Exchange;
import com.aionemu.gameserver.model.trade.ExchangeItem;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.taskmanager.tasks.TemporaryTradeTimeTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class ExchangeService {

	private static final Logger log = LoggerFactory.getLogger("EXCHANGE_LOG");

	private final Map<Integer, Exchange> exchanges = new ConcurrentHashMap<>();

	public static ExchangeService getInstance() {
		return SingletonHolder.instance;
	}

	private ExchangeService() {
	}

	public void registerExchange(Player player1, Player player2) {
		if (!validateParticipants(player1, player2))
			return;

		exchanges.put(player1.getObjectId(), new Exchange(player1, player2));
		exchanges.put(player2.getObjectId(), new Exchange(player2, player1));

		PacketSendUtility.sendPacket(player2, new SM_EXCHANGE_REQUEST(player1.getName()));
		PacketSendUtility.sendPacket(player1, new SM_EXCHANGE_REQUEST(player2.getName()));
	}

	private boolean validateParticipants(Player player1, Player player2) {
		return PlayerRestrictions.canTrade(player1) && PlayerRestrictions.canTrade(player2);
	}

	private Player getCurrentParter(Player player) {
		Exchange exchange = exchanges.get(player.getObjectId());
		return exchange != null ? exchange.getTargetPlayer() : null;
	}

	private Exchange getCurrentExchange(Player player) {
		return exchanges.get(player.getObjectId());
	}

	public Exchange getCurrentParnterExchange(Player player) {
		Player partner = getCurrentParter(player);
		return partner != null ? getCurrentExchange(partner) : null;
	}

	public boolean isPlayerInExchange(Player player) {
		return getCurrentExchange(player) != null;
	}

	public void addKinah(Player activePlayer, long itemCount) {
		Exchange currentExchange = getCurrentExchange(activePlayer);
		if (currentExchange == null || currentExchange.isLocked())
			return;

		if (itemCount < 1)
			return;

		// count total amount in inventory
		long availableCount = activePlayer.getInventory().getKinah();

		// count amount that was already added to exchange
		availableCount -= currentExchange.getKinahCount();

		long countToAdd = availableCount > itemCount ? itemCount : availableCount;

		if (countToAdd > 0) {
			Player partner = getCurrentParter(activePlayer);
			PacketSendUtility.sendPacket(activePlayer, new SM_EXCHANGE_ADD_KINAH(countToAdd, 0));
			PacketSendUtility.sendPacket(partner, new SM_EXCHANGE_ADD_KINAH(countToAdd, 1));
			currentExchange.addKinah(countToAdd);
		}
	}

	public void addItem(Player activePlayer, int itemObjId, long itemCount) {
		Item item = activePlayer.getInventory().getItemByObjId(itemObjId);
		if (item == null)
			return;

		Player partner = getCurrentParter(activePlayer);
		if (partner == null)
			return;
		if (item.getPackCount() <= 0 && !item.isTradeable() && !TemporaryTradeTimeTask.getInstance().canTrade(item, partner.getObjectId())) {
			if (!item.isLegionTradeable() || activePlayer.getLegion() == null || !activePlayer.getLegion().equals(partner.getLegion()))
				return;
		}

		if (itemCount < 1)
			return;

		if (itemCount > item.getItemCount())
			return;

		Exchange currentExchange = getCurrentExchange(activePlayer);

		if (currentExchange == null)
			return;

		if (currentExchange.isLocked())
			return;

		if (currentExchange.isExchangeListFull())
			return;

		if (!AdminService.getInstance().canOperate(activePlayer, partner, item, "trade"))
			return;

		ExchangeItem exchangeItem = currentExchange.getItems().get(item.getObjectId());

		long actuallAddCount = 0;
		// item was not added previosly
		if (exchangeItem == null) {
			Item newItem = null;
			if (itemCount < item.getItemCount()) {
				newItem = ItemFactory.newItem(item.getItemId(), itemCount);
			} else {
				newItem = item;
			}
			exchangeItem = new ExchangeItem(itemObjId, itemCount, newItem);
			currentExchange.addItem(itemObjId, exchangeItem);
			actuallAddCount = itemCount;
		}
		// item was already added
		else {
			// if player add item count that is more than possible
			// happens with exploits
			if (item.getItemCount() == exchangeItem.getItemCount())
				return;

			long possibleToAdd = item.getItemCount() - exchangeItem.getItemCount();
			actuallAddCount = itemCount > possibleToAdd ? possibleToAdd : itemCount;
			exchangeItem.addCount(actuallAddCount);
		}

		if (!item.getItemTemplate().isStackable() || item.getItemCount() == exchangeItem.getItemCount()) {
			PacketSendUtility.sendPacket(activePlayer, new SM_DELETE_ITEM(itemObjId, ItemPacketService.ItemDeleteType.PUT_TO_EXCHANGE));
		} else {
			Item fakeItem = new Item(itemObjId, item.getItemTemplate());
			fakeItem.setItemCount(item.getItemCount() - exchangeItem.getItemCount());
			PacketSendUtility.sendPacket(activePlayer, new SM_INVENTORY_UPDATE_ITEM(activePlayer, fakeItem,
				ItemPacketService.ItemUpdateType.PUT_TO_EXCHANGE));
		}

		PacketSendUtility.sendPacket(activePlayer, new SM_EXCHANGE_ADD_ITEM(0, exchangeItem.getItem(), activePlayer));
		PacketSendUtility.sendPacket(partner, new SM_EXCHANGE_ADD_ITEM(1, exchangeItem.getItem(), partner));
	}

	public void lockExchange(Player activePlayer) {
		Exchange exchange = getCurrentExchange(activePlayer);
		if (exchange != null) {
			exchange.lock();
			Player currentParter = getCurrentParter(activePlayer);
			PacketSendUtility.sendPacket(currentParter, new SM_EXCHANGE_CONFIRMATION(3));
		}
	}

	public void cancelExchange(Player activePlayer) {
		Player currentPartner = getCurrentParter(activePlayer);
		returnItems(activePlayer);

		if (currentPartner != null) {
			returnItems(currentPartner);
			PacketSendUtility.sendPacket(currentPartner, new SM_EXCHANGE_CONFIRMATION(1));
		}

		cleanUpExchanges(true, activePlayer, currentPartner);
	}

	private void returnItems(Player player) {
		Exchange exchange = getCurrentExchange(player);
		if (exchange == null) {
			return;
		}
		if (!exchange.getItems().isEmpty()) {
			for (ExchangeItem exItem : exchange.getItems().values()) {
				Item realItem = player.getInventory().getItemByObjId(exItem.getItemObjId());
				if (realItem == null) {
					log.warn("Player " + player.getName() + " is trying to return fake item on exchange cancel!");
					return;
				}
				if (realItem.getItemCount() == exItem.getItemCount()) {
					PacketSendUtility.sendPacket(player, new SM_INVENTORY_ADD_ITEM(Arrays.asList(realItem), player, ItemPacketService.ItemAddType.PLAYER_EXCHANGE_GET_BACK));
				} else {
					PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, realItem, ItemPacketService.ItemUpdateType.INC_PLAYER_EXCHANGE_GET_BACK));
				}
			}
			PacketSendUtility.sendPacket(player, SM_CUBE_UPDATE.cubeSize(StorageType.CUBE, player));
		}
	}

	public void confirmExchange(Player activePlayer) {
		if (activePlayer == null || !activePlayer.isOnline())
			return;

		Exchange currentExchange = getCurrentExchange(activePlayer);

		// TODO: Why is exchange null =/
		if (currentExchange == null)
			return;
		currentExchange.confirm();

		Player currentPartner = getCurrentParter(activePlayer);
		PacketSendUtility.sendPacket(currentPartner, new SM_EXCHANGE_CONFIRMATION(2));

		if (getCurrentExchange(currentPartner).isConfirmed()) {
			performTrade(activePlayer, currentPartner);
		}
	}

	private void performTrade(Player activePlayer, Player currentPartner) {
		Exchange exchange1 = getCurrentExchange(activePlayer);
		Exchange exchange2 = getCurrentExchange(currentPartner);

		if (!validateExchange(activePlayer, currentPartner)) {
			if (!validateInventorySize(currentPartner, exchange1))
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_EXCHANGE_CANT_EXCHANGE_HEAVY_TO_ADD_EXCHANGE_ITEM());
			else
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_PARTNER_TOO_HEAVY_TO_EXCHANGE());
			cleanUpExchanges(true, activePlayer, currentPartner);
			return;
		}

		if (!removeItemsFromInventory(activePlayer, exchange1) || !removeItemsFromInventory(currentPartner, exchange2)) {
			cleanUpExchanges(true, activePlayer, currentPartner);
			AuditLogger.log(activePlayer, "tried to exploit kinah exchange with partner: " + currentPartner);
			return;
		}

		PacketSendUtility.sendPacket(activePlayer, new SM_EXCHANGE_CONFIRMATION(0));
		PacketSendUtility.sendPacket(currentPartner, new SM_EXCHANGE_CONFIRMATION(0));

		putItemToInventory(activePlayer, currentPartner, exchange1, exchange2);
		putItemToInventory(currentPartner, activePlayer, exchange2, exchange1);
		InventoryDAO.store(exchange1.getActiveplayer());
		InventoryDAO.store(exchange2.getActiveplayer());

		cleanUpExchanges(false, activePlayer, currentPartner);
	}

	private void cleanUpExchanges(boolean releaseIds, Player... players) {
		for (Player player : players) {
			if (player == null)
				continue;

			Exchange exchange = exchanges.remove(player.getObjectId());
			if (exchange != null && releaseIds) {
				for (ExchangeItem item : exchange.getItems().values()) {
					if (item.getItemObjId() != item.getItem().getObjectId() && player.getInventory().getItemByObjId(item.getItem().getObjectId()) == null)
						IDFactory.getInstance().releaseId(item.getItem().getObjectId()); // release ID if it was a newly allocated one
				}
			}
		}
	}

	private boolean removeItemsFromInventory(Player player, Exchange exchange) {
		Storage inventory = player.getInventory();

		for (ExchangeItem exchangeItem : exchange.getItems().values()) {
			Item item = exchangeItem.getItem();
			Item itemInInventory = inventory.getItemByObjId(exchangeItem.getItemObjId());
			if (itemInInventory == null) {
				AuditLogger.log(player, "tried to trade not existing item");
				return false;
			}

			long itemCount = exchangeItem.getItemCount();

			if (itemCount < itemInInventory.getItemCount()) {
				inventory.decreaseItemCount(itemInInventory, itemCount);
			} else {
				// remove from source inventory only
				inventory.remove(itemInInventory);
				exchangeItem.setItem(itemInInventory);
				// release when only part stack was added in the beginning -> full stack in the end
				if (item.getObjectId() != exchangeItem.getItemObjId()) {
					IDFactory.getInstance().releaseId(item.getObjectId());
				}
				PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(itemInInventory.getObjectId()));
			}
		}
		return player.getInventory().tryDecreaseKinah(exchange.getKinahCount());
	}

	private boolean validateExchange(Player activePlayer, Player currentPartner) {
		Exchange exchange1 = getCurrentExchange(activePlayer);
		Exchange exchange2 = getCurrentExchange(currentPartner);
		boolean activePlayerCheck = validateInventorySize(activePlayer, exchange2);
		boolean currentPartnerCheck = validateInventorySize(currentPartner, exchange1);
		if(!activePlayerCheck) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_EXCHANGE_CANT_EXCHANGE_HEAVY_TO_ADD_EXCHANGE_ITEM());
			PacketSendUtility.sendPacket(currentPartner, SM_SYSTEM_MESSAGE.STR_PARTNER_TOO_HEAVY_TO_EXCHANGE());
		}	else if(!currentPartnerCheck) {
			PacketSendUtility.sendPacket(currentPartner, SM_SYSTEM_MESSAGE.STR_EXCHANGE_CANT_EXCHANGE_HEAVY_TO_ADD_EXCHANGE_ITEM());
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_PARTNER_TOO_HEAVY_TO_EXCHANGE());
		}
		return activePlayerCheck && currentPartnerCheck;
	}

	private boolean validateInventorySize(Player activePlayer, Exchange exchange) {
		int numberOfFreeSlots = activePlayer.getInventory().getFreeSlots();
		return numberOfFreeSlots >= exchange.getItems().size();
	}

	private void putItemToInventory(Player giver, Player partner, Exchange exchange1, Exchange exchange2) {
		for (ExchangeItem exchangeItem : exchange1.getItems().values()) {
			Item itemToPut = exchangeItem.getItem();
			itemToPut.setEquipmentSlot(0);
			if (itemToPut.getPackCount() > 0) // unpack
				itemToPut.setPackCount(itemToPut.getPackCount() * -1);
			partner.getInventory().add(itemToPut, ItemPacketService.ItemAddType.PLAYER_EXCHANGE_GET);
			if (LoggingConfig.LOG_PLAYER_EXCHANGE)
				log.info("Player " + giver.getName() + " exchanged item " + itemToPut.getItemId() + " [" + itemToPut.getItemName() + "] (count: "
					+ itemToPut.getItemCount() + ") with player " + partner.getName());
		}
		long kinahToExchange = exchange1.getKinahCount();
		if (kinahToExchange > 0) {
			partner.getInventory().increaseKinah(kinahToExchange);
			if (LoggingConfig.LOG_PLAYER_EXCHANGE)
				log.info("Player " + giver.getName() + " exchanged " + kinahToExchange + " Kinah with player " + partner.getName());
		}
	}

	private static class SingletonHolder {

		protected static final ExchangeService instance = new ExchangeService();
	}
}
