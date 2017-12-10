package com.aionemu.gameserver.model.items.storage;

import java.util.List;
import java.util.Queue;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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

	/**
	 * @param player
	 */
	void setOwner(Player player);

	/**
	 * @return current kinah count
	 */
	long getKinah();

	/**
	 * @return kinah item or null if storage never had kinah
	 */
	Item getKinahItem();

	/**
	 * @return
	 */
	StorageType getStorageType();

	/**
	 * @param amount
	 */
	void increaseKinah(long amount);

	/**
	 * @param amount
	 * @param updateType
	 */
	void increaseKinah(long amount, ItemUpdateType updateType);

	/**
	 * @param amount
	 * @return
	 */
	boolean tryDecreaseKinah(long amount);

	boolean tryDecreaseKinah(long amount, ItemUpdateType updateType);

	/**
	 * @param amount
	 */
	void decreaseKinah(long amount);

	/**
	 * @param amount
	 * @param updateType
	 */
	void decreaseKinah(long amount, ItemUpdateType updateType);

	/**
	 * @param item
	 * @param count
	 * @return
	 */
	long increaseItemCount(Item item, long count);

	/**
	 * @param item
	 * @param count
	 * @param updateType
	 * @return
	 */
	long increaseItemCount(Item item, long count, ItemUpdateType updateType);

	/**
	 * @param item
	 * @param count
	 * @return
	 */
	long decreaseItemCount(Item item, long count);

	/**
	 * @param item
	 * @param count
	 * @param updateType
	 * @return
	 */
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

	/**
	 * @param item
	 * @return
	 */
	Item remove(Item item);

	/**
	 * @param item
	 * @return
	 */
	Item delete(Item item);

	/**
	 * @param item
	 * @param deleteType
	 * @return
	 */
	Item delete(Item item, ItemDeleteType deleteType);

	/**
	 * @param itemId
	 * @param count
	 * @return
	 */
	boolean decreaseByItemId(int itemId, long count);

	boolean decreaseByItemId(int itemId, long count, QuestStatus questStatus);

	/**
	 * @param itemObjId
	 * @param count
	 * @return
	 */
	boolean decreaseByObjectId(int itemObjId, long count);

	/**
	 * @param itemObjId
	 * @param count
	 * @param updateType
	 * @return
	 */
	boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType);

	boolean decreaseByObjectId(int itemObjId, long count, QuestStatus questStatus);

	/**
	 * @param itemId
	 * @return
	 */
	Item getFirstItemByItemId(int itemId);

	/**
	 * @return
	 */
	List<Item> getItemsWithKinah();

	/**
	 * @return
	 */
	List<Item> getItems();

	/**
	 * @param itemId
	 * @return
	 */
	List<Item> getItemsByItemId(int itemId);

	/**
	 * @param itemObjId
	 * @return
	 */
	Item getItemByObjId(int itemObjId);

	/**
	 * @param itemId
	 * @return
	 */
	long getItemCountByItemId(int itemId);

	/**
	 * @return
	 */
	boolean isFull();

	/**
	 * @return
	 */
	int getFreeSlots();

	/**
	 * @return
	 */
	int getLimit();

	int getRowLength();

	/**
	 * @return
	 */
	int size();

	/**
	 * @return
	 */
	Queue<Item> getDeletedItems();

	/**
	 * @param item
	 */
	void onLoadHandler(Item item);

}
