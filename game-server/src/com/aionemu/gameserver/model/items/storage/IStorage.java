package com.aionemu.gameserver.model.items.storage;

import java.util.List;
import java.util.Queue;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;

/**
 * Public interface for Storage, later will rename probably
 * 
 * @author ATracer
 */
public interface IStorage extends Persistable {

	void setOwner(Player player);

	long getKinah();

	/**
	 * @return kinah item or null if storage never had kinah
	 */
	Item getKinahItem();

	StorageType getStorageType();

	void increaseKinah(long amount);

	void increaseKinah(long amount, ItemUpdateType updateType);

	boolean tryDecreaseKinah(long amount);

	boolean tryDecreaseKinah(long amount, ItemUpdateType updateType);

	void decreaseKinah(long amount);

	void decreaseKinah(long amount, ItemUpdateType updateType);

	long increaseItemCount(Item item, long count);

	long increaseItemCount(Item item, long count, ItemUpdateType updateType);

	long decreaseItemCount(Item item, long count);

	long decreaseItemCount(Item item, long count, ItemUpdateType updateType);

	long decreaseItemCount(Item item, long count, ItemUpdateType updateType, QuestStatus questStatus);

	/**
	 * Add operation should be used for new items incoming into storage from outside
	 */
	Item add(Item item);

	Item add(Item item, ItemAddType addType);

	/**
	 * Put operation is used in some operations like unequip
	 */
	Item put(Item item);

	Item remove(Item item);

	Item delete(Item item);

	Item delete(Item item, ItemDeleteType deleteType);

	boolean decreaseByItemId(int itemId, long count);

	boolean decreaseByItemId(int itemId, long count, QuestStatus questStatus);

	boolean decreaseByObjectId(int itemObjId, long count);

	boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType);

	boolean decreaseByObjectId(int itemObjId, long count, QuestStatus questStatus);

	Item getFirstItemByItemId(int itemId);

	List<Item> getItemsWithKinah();

	List<Item> getItems();

	List<Item> getItemsByItemId(int itemId);

	Item getItemByObjId(int itemObjId);

	long getItemCountByItemId(int itemId);

	boolean isFull();

	int getFreeSlots();

	int getLimit();

	int getRowLength();

	int size();

	Queue<Item> getDeletedItems();

	void onLoadHandler(Item item);

	default SM_SYSTEM_MESSAGE getStorageIsFullMessage() {
		return switch (getStorageType()) {
			case CUBE -> SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY();
			case REGULAR_WAREHOUSE, ACCOUNT_WAREHOUSE, LEGION_WAREHOUSE -> SM_SYSTEM_MESSAGE.STR_WAREHOUSE_DEPOSIT_FULL_BASKET();
			case PET_BAG_6, PET_BAG_12, PET_BAG_18, PET_BAG_24, CASH_PET_BAG_12, CASH_PET_BAG_18, CASH_PET_BAG_30, CASH_PET_BAG_24, PET_BAG_30,
					 CASH_PET_BAG_26, CASH_PET_BAG_32, CASH_PET_BAG_34 -> SM_SYSTEM_MESSAGE.STR_WAREHOUSE_TOO_MANY_ITEMS_TOYPET_WAREHOUSE();
			case HOUSE_STORAGE_01, HOUSE_STORAGE_02, HOUSE_STORAGE_03, HOUSE_STORAGE_04, HOUSE_STORAGE_05, HOUSE_STORAGE_06, HOUSE_STORAGE_07,
					 HOUSE_STORAGE_08, HOUSE_STORAGE_09, HOUSE_STORAGE_10, HOUSE_STORAGE_11, HOUSE_STORAGE_12, HOUSE_STORAGE_13, HOUSE_STORAGE_14,
					 HOUSE_STORAGE_15, HOUSE_STORAGE_16, HOUSE_STORAGE_17, HOUSE_STORAGE_18, HOUSE_STORAGE_19, HOUSE_STORAGE_20 ->
				SM_SYSTEM_MESSAGE.STR_HOUSING_WAREHOUSE_TOO_MANY_ITEMS_WAREHOUSE();
			case BROKER -> SM_SYSTEM_MESSAGE.STR_VENDOR_FULL_ITEM();
			case MAILBOX -> SM_SYSTEM_MESSAGE.STR_MAIL_SEND_FULL_BASKET();
		};
	}
}
