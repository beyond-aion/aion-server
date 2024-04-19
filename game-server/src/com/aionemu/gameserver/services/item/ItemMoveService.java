package com.aionemu.gameserver.services.item;

import static com.aionemu.gameserver.services.item.ItemPacketService.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class ItemMoveService {

	private static final Logger log = LoggerFactory.getLogger(ItemMoveService.class);

	public static void moveItem(Player player, int itemObjId, byte sourceStorageType, byte destinationStorageType, short slot) {
		IStorage sourceStorage = player.getStorage(sourceStorageType);
		if (sourceStorage == null) {
			log.error(player + " tried to move itemObjId " + itemObjId + " from unknown sourceStorageType: " + sourceStorageType);
			return;
		}
		Item item = sourceStorage.getItemByObjId(itemObjId);
		if (item == null)
			return;

		IStorage targetStorage = player.getStorage(destinationStorageType);
		if (targetStorage == null) {
			log.error(player + " tried to move itemObjId " + itemObjId + " to unknown destinationStorageType: " + destinationStorageType);
			return;
		}

		if (sourceStorageType == destinationStorageType) {
			if (item.getEquipmentSlot() != slot)
				moveInSameStorage(sourceStorage, item, slot);
			return;
		}
		if (ItemRestrictionService.isItemRestrictedTo(player, item, destinationStorageType)
			|| ItemRestrictionService.isItemRestrictedFrom(player, item, sourceStorageType)
			|| player.isTrading()
			|| GameServer.isShuttingDownSoon()) {
			sendStorageUpdatePacket(player, StorageType.getStorageTypeById(sourceStorageType), item, ItemAddType.ALL_SLOT);
			if (GameServer.isShuttingDownSoon())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DISABLE("Shutdown Progress"));
			return;
		}

		if (sourceStorageType == StorageType.LEGION_WAREHOUSE.getId() || destinationStorageType == StorageType.LEGION_WAREHOUSE.getId()) {
			LegionService.getInstance().addWHItemHistory(player, item.getItemId(), item.getItemCount(), sourceStorage, targetStorage);
		}
		if (slot == -1) {
			if (item.getItemTemplate().isStackable()) {
				List<Item> sameItems = targetStorage.getItemsByItemId(item.getItemId());
				for (Item sameItem : sameItems) {
					long itemCount = item.getItemCount();
					if (itemCount == 0) {
						break;
					}
					// we can merge same stackable items
					ItemSplitService.mergeStacks(sourceStorage, targetStorage, item, sameItem, itemCount);
				}
			}
		}
		if (!targetStorage.isFull() && item.getItemCount() > 0) {
			sourceStorage.remove(item);
			sendItemDeletePacket(player, StorageType.getStorageTypeById(sourceStorageType), item, ItemDeleteType.MOVE);
			item.setEquipmentSlot(slot);
			targetStorage.add(item);
		}
	}

	private static void moveInSameStorage(IStorage storage, Item item, short slot) {
		storage.setPersistentState(PersistentState.UPDATE_REQUIRED);
		item.setEquipmentSlot(slot);
		item.setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public static void switchItemsInStorages(Player player, byte sourceStorageType, int sourceItemObjId, byte replaceStorageType, int replaceItemObjId) {
		IStorage sourceStorage = player.getStorage(sourceStorageType);
		IStorage replaceStorage = player.getStorage(replaceStorageType);

		Item sourceItem = sourceStorage.getItemByObjId(sourceItemObjId);
		if (sourceItem == null)
			return;

		Item replaceItem = replaceStorage.getItemByObjId(replaceItemObjId);
		if (replaceItem == null)
			return;

		// restrictions checks
		if (ItemRestrictionService.isItemRestrictedFrom(player, sourceItem, sourceStorageType)
			|| ItemRestrictionService.isItemRestrictedFrom(player, replaceItem, replaceStorageType)
			|| ItemRestrictionService.isItemRestrictedTo(player, sourceItem, replaceStorageType)
			|| ItemRestrictionService.isItemRestrictedTo(player, replaceItem, sourceStorageType)
			|| player.isTrading() 
			|| GameServer.isShuttingDownSoon()) {
			sendStorageUpdatePacket(player, StorageType.getStorageTypeById(sourceStorageType), sourceItem, ItemAddType.ALL_SLOT);
			sendStorageUpdatePacket(player, StorageType.getStorageTypeById(replaceStorageType), replaceItem, ItemAddType.ALL_SLOT);
			if (GameServer.isShuttingDownSoon())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DISABLE("Shutdown Progress"));
			return;
		}

		long sourceSlot = sourceItem.getEquipmentSlot();
		long replaceSlot = replaceItem.getEquipmentSlot();

		sourceItem.setEquipmentSlot(replaceSlot);
		replaceItem.setEquipmentSlot(sourceSlot);

		sourceStorage.remove(sourceItem);
		replaceStorage.remove(replaceItem);

		// correct UI update order is 1)delete items 2) add items
		sendItemDeletePacket(player, StorageType.getStorageTypeById(sourceStorageType), sourceItem, ItemDeleteType.MOVE);
		sendItemDeletePacket(player, StorageType.getStorageTypeById(replaceStorageType), replaceItem, ItemDeleteType.MOVE);
		sourceStorage.add(replaceItem);
		replaceStorage.add(sourceItem);
	}
}
