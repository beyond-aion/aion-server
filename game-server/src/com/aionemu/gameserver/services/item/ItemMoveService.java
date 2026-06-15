package com.aionemu.gameserver.services.item;

import static com.aionemu.gameserver.services.item.ItemPacketService.*;

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
		if (ItemRestrictionService.isItemRestrictedTo(player, item, targetStorage.getStorageType())
			|| ItemRestrictionService.isItemRestrictedFrom(player, item, sourceStorage.getStorageType())
			|| player.isTrading()
			|| GameServer.isShuttingDownSoon()) {
			sendItemUnlockPacket(player, item);
			if (GameServer.isShuttingDownSoon())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DISABLE("Shutdown Progress"));
			return;
		}

		if (sourceStorageType == StorageType.LEGION_WAREHOUSE.getId() || destinationStorageType == StorageType.LEGION_WAREHOUSE.getId()) {
			LegionService.getInstance().addWHItemHistory(player, item.getItemId(), item.getItemCount(), sourceStorage, targetStorage);
		}
		if (slot == -1) {
			if (item.getItemTemplate().isStackable()) {
				for (Item targetStack : targetStorage.getItemsByItemId(item.getItemId())) {
					ItemSplitService.mergeStacks(sourceStorage, targetStorage, item, targetStack, item.getItemCount());
					if (item.getItemCount() == 0) {
						return;
					}
				}
			}
		}
		if (targetStorage.isFull()) {
			PacketSendUtility.sendPacket(player, targetStorage.getStorageIsFullMessage());
			sendItemUnlockPacket(player, item);
			return;
		}
		sourceStorage.remove(item);
		sendItemDeletePacket(player, sourceStorage.getStorageType(), item, ItemDeleteType.MOVE);
		item.setEquipmentSlot(slot);
		targetStorage.add(item);
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
		if (ItemRestrictionService.isItemRestrictedFrom(player, sourceItem, sourceStorage.getStorageType())
			|| ItemRestrictionService.isItemRestrictedFrom(player, replaceItem, replaceStorage.getStorageType())
			|| ItemRestrictionService.isItemRestrictedTo(player, sourceItem, replaceStorage.getStorageType())
			|| ItemRestrictionService.isItemRestrictedTo(player, replaceItem, sourceStorage.getStorageType())
			|| player.isTrading() 
			|| GameServer.isShuttingDownSoon()) {
			sendItemUnlockPacket(player, sourceItem);
			sendItemUnlockPacket(player, replaceItem);
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
		sendItemDeletePacket(player, sourceStorage.getStorageType(), sourceItem, ItemDeleteType.MOVE);
		sendItemDeletePacket(player, replaceStorage.getStorageType(), replaceItem, ItemDeleteType.MOVE);
		sourceStorage.add(replaceItem);
		replaceStorage.add(sourceItem);
	}
}
