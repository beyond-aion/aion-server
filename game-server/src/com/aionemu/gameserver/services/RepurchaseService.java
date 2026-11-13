package com.aionemu.gameserver.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.trade.RepurchaseList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author xTz
 */
public class RepurchaseService {

	private final Map<Integer, Set<Item>> repurchaseItems = new ConcurrentHashMap<>();

	private RepurchaseService() {
	}

	/**
	 * Publishes sales already saved at {@link StorageType#REPURCHASE}, under the player's item save lock.
	 */
	public void addRepurchaseItems(Player player, List<Item> items) {
		Set<Item> newItems = ConcurrentHashMap.newKeySet();
		newItems.addAll(items);
		Set<Item> oldItems = repurchaseItems.put(player.getObjectId(), newItems);
		if (oldItems != null) // the shop only remembers the latest sale; older items can no longer be bought back
			deleteItems(player, oldItems);
	}

	/**
	 * Delete all repurchase items for this player
	 */
	public void removeRepurchaseItems(Player player) {
		synchronized (player.getItemSaveLock()) {
			Set<Item> items = repurchaseItems.remove(player.getObjectId());
			if (items != null)
				deleteItems(player, items);
		}
	}

	public Set<Item> getRepurchaseItems(int playerObjectId) {
		return repurchaseItems.getOrDefault(playerObjectId, Collections.emptySet());
	}

	public boolean canRepurchase(Player player, int itemObjectId) {
		return getRepurchaseItems(player.getObjectId()).stream().anyMatch(item -> item.getObjectId() == itemObjectId);
	}

	public void repurchaseFromShop(Player player, RepurchaseList repurchaseList) {
		if (!PlayerRestrictions.canTrade(player))
			return;
		synchronized (player.getItemSaveLock()) {
			Set<Item> items = repurchaseItems.get(player.getObjectId());
			if (items == null)
				return;
			Storage inventory = player.getInventory();
			for (int itemObjectId : repurchaseList.getRepurchaseItems()) {
				Item item = items.stream().filter(candidate -> candidate.getObjectId() == itemObjectId).findAny().orElse(null);
				if (item == null)
					continue;
				if (inventory.isFull(item.getItemTemplate().getExtraInventoryId())) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR());
					break;
				}
				if (inventory.getKinah() < item.getRepurchasePrice()) {
					AuditLogger.log(player, "tried to repurchase item " + item.getItemId() + ", count: " + item.getItemCount() + " without kinah");
					continue;
				}

				items.remove(item);
				if (!inventory.repurchase(player, item))
					items.add(item);
			}
		}
	}

	private void deleteItems(Player player, Collection<Item> items) {
		if (items.isEmpty())
			return;
		List<Item> toDelete = new ArrayList<>(items);
		for (Item item : toDelete)
			item.setPersistentState(PersistentState.DELETED);
		if (!InventoryDAO.storeAtomically(toDelete, player.getObjectId())) {
			Storage inventory = player.getInventory();
			synchronized (inventory) {
				inventory.getDeletedItems().addAll(toDelete);
				inventory.setPersistentState(PersistentState.UPDATE_REQUIRED);
			}
		}
	}

	public static RepurchaseService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {

		protected static final RepurchaseService INSTANCE = new RepurchaseService();
	}

}
