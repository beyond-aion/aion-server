package com.aionemu.gameserver.services.item;

import static com.aionemu.gameserver.services.item.ItemPacketService.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.ItemStorage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.services.ExchangeService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;

/**
 * @author ATracer
 */
public class ItemMoveService {

	private static final Logger log = LoggerFactory.getLogger(ItemMoveService.class);

	public static void moveItem(Player player, int itemObjId, byte sourceStorageType, byte destinationStorageType, short slot) {
		if (ExchangeService.getInstance().isPlayerInExchange(player))
			return;

		IStorage sourceStorage = player.getStorage(sourceStorageType);
		Item item = player.getStorage(sourceStorageType).getItemByObjId(itemObjId);

		if (item == null)
			return;

		IStorage targetStorage = player.getStorage(destinationStorageType);
		if (targetStorage == null) {
			log.error("Try moving item to null storage! destinationStorageType:" + destinationStorageType);
			return;
		}

		if (sourceStorageType == destinationStorageType) {
			if (item.getEquipmentSlot() != slot)
				moveInSameStorage(sourceStorage, item, slot);
			return;
		} else if (ItemRestrictionService.isItemRestrictedTo(player, item, destinationStorageType)
			|| ItemRestrictionService.isItemRestrictedFrom(player, item, sourceStorageType)) {
			sendStorageUpdatePacket(player, StorageType.getStorageTypeById(sourceStorageType), item, ItemAddType.ALL_SLOT);
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
			item.setEquipmentSlot(ItemStorage.FIRST_AVAILABLE_SLOT);
			targetStorage.add(item);
		}
	}

	/**
	 * @param storage
	 * @param item
	 * @param slot
	 */
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
			|| ItemRestrictionService.isItemRestrictedTo(player, replaceItem, sourceStorageType))
			return;

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
