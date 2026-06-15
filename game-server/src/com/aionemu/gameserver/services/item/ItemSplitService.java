package com.aionemu.gameserver.services.item;

import static com.aionemu.gameserver.services.item.ItemPacketService.sendStorageUpdatePacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class ItemSplitService {

	private static final Logger log = LoggerFactory.getLogger(ItemSplitService.class);

	/**
	 * Move part of stack into different slot
	 */
	public static void splitItem(Player player, int itemObjId, int destinationObjId, long splitAmount, short slotNum, byte sourceStorageType,
		byte destinationStorageType) {
		if (splitAmount <= 0) {
			return;
		}
		if (player.isTrading()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INVENTORY_SPLIT_DURING_TRADE());
			return;
		}

		IStorage sourceStorage = player.getStorage(sourceStorageType);
		IStorage destStorage = player.getStorage(destinationStorageType);
		if (sourceStorage == null || destStorage == null) {
			log.warn(String.format("storage null playerName sourceStorage destStorage %s %d %d", player.getName(), sourceStorageType,
				destinationStorageType));
			return;
		}
		Item sourceItem = sourceStorage.getItemByObjId(itemObjId);
		Item targetItem = destStorage.getItemByObjId(destinationObjId);

		if (sourceItem == null) {
			sourceItem = sourceStorage.getKinahItem();
			if (sourceItem == null || sourceItem.getObjectId() != itemObjId) {
				log.warn(String.format("CHECKPOINT: attempt to split null item %d %d %d", itemObjId, splitAmount, slotNum));
				return;
			}
		}

		if (sourceStorageType != destinationStorageType
			&& (ItemRestrictionService.isItemRestrictedTo(player, sourceItem, destStorage.getStorageType()) || ItemRestrictionService.isItemRestrictedFrom(
				player, sourceItem, sourceStorage.getStorageType()))) {
			sendStorageUpdatePacket(player, sourceStorage.getStorageType(), sourceItem);
			return;
		}

		// To move kinah from inventory to warehouse and vice versa client using split item packet
		if (sourceItem.getItemTemplate().isKinah()) {
			moveKinah(player, sourceStorage, splitAmount);
			return;
		}

		if (targetItem == null) {
			if (destStorage.isFull()) {
				PacketSendUtility.sendPacket(player, destStorage.getStorageIsFullMessage());
				return;
			}
			long oldItemCount = sourceItem.getItemCount() - splitAmount;
			if (sourceItem.getItemCount() < splitAmount || oldItemCount == 0) {
				return;
			}
			if (sourceStorageType != destinationStorageType) {
				LegionService.getInstance().addWHItemHistory(player, sourceItem.getItemId(), splitAmount, sourceStorage, destStorage);
			}
			Item newItem = ItemFactory.newItem(sourceItem.getItemTemplate().getTemplateId(), splitAmount);
			if (sourceStorageType == destinationStorageType)
				newItem.setEquipmentSlot(slotNum);
			sourceStorage.decreaseItemCount(sourceItem, splitAmount, sourceStorageType == destinationStorageType ? ItemUpdateType.DEC_ITEM_SPLIT
				: ItemUpdateType.DEC_ITEM_SPLIT_MOVE);
			PacketSendUtility.sendPacket(player, SM_CUBE_UPDATE.cubeSize(sourceStorage.getStorageType(), player));
			if (destStorage.add(newItem) == null) {
				// if item was not added - we can release its id
				IDFactory.getInstance().releaseId(newItem.getObjectId());
			}
		} else if (targetItem.getItemId() == sourceItem.getItemId()) {
			if (sourceStorageType != destinationStorageType) {
				LegionService.getInstance().addWHItemHistory(player, sourceItem.getItemId(), splitAmount, sourceStorage, destStorage);
			}
			mergeStacks(sourceStorage, destStorage, sourceItem, targetItem, splitAmount);
		}
	}

	/**
	 * Merge 2 stacks with simple validation
	 */
	public static void mergeStacks(IStorage sourceStorage, IStorage destStorage, Item sourceItem, Item targetItem, long count) {
		if (sourceItem.getItemCount() >= count) {
			long freeCount = targetItem.getFreeCount();
			count = count > freeCount ? freeCount : count;
			long leftCount = destStorage.increaseItemCount(targetItem, count,
				sourceStorage.getStorageType() == destStorage.getStorageType() ? ItemUpdateType.INC_ITEM_MERGE : ItemUpdateType.INC_ITEM_COLLECT);
			sourceStorage.decreaseItemCount(sourceItem, count - leftCount,
				sourceStorage.getStorageType() == destStorage.getStorageType() ? ItemUpdateType.DEC_ITEM_SPLIT : ItemUpdateType.DEC_ITEM_SPLIT_MOVE);
		}

	}

	private static void moveKinah(Player player, IStorage source, long splitAmount) {
		if (source.getKinah() < splitAmount)
			return;
		switch (source.getStorageType()) {
			case CUBE: {
				IStorage destination = player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId());
				long chksum = (source.getKinah() - splitAmount) + (destination.getKinah() + splitAmount);

				if (chksum != source.getKinah() + destination.getKinah())
					return;

				updateKinahCount(source, splitAmount, destination);
				break;
			}

			case ACCOUNT_WAREHOUSE: {
				IStorage destination = player.getStorage(StorageType.CUBE.getId());
				long chksum = (source.getKinah() - splitAmount) + (destination.getKinah() + splitAmount);

				if (chksum != source.getKinah() + destination.getKinah())
					return;

				updateKinahCount(source, splitAmount, destination);
				break;
			}
		}
	}

	private static void updateKinahCount(IStorage source, long splitAmount, IStorage destination) {
		source.decreaseKinah(splitAmount, ItemUpdateType.DEC_ITEM_SPLIT);
		destination.increaseKinah(splitAmount, ItemUpdateType.INC_KINAH_MERGE);
	}
}
