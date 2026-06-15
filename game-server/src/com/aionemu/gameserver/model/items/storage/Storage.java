package com.aionemu.gameserver.model.items.storage;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;

/**
 * @author KID, ATracer
 */
public abstract class Storage implements IStorage {

	private static final Logger log = LoggerFactory.getLogger("ITEM_LOG");
	private ItemStorage itemStorage;
	private Item kinahItem;
	private StorageType storageType;
	private Queue<Item> deletedItems;
	/**
	 * Can be of 2 types: UPDATED and UPDATE_REQUIRED
	 */
	private PersistentState persistentState = PersistentState.UPDATED;

	public Storage(StorageType storageType) {
		this(storageType, true);
	}

	public Storage(StorageType storageType, boolean withDeletedItems) {
		itemStorage = new ItemStorage(storageType);
		this.storageType = storageType;
		if (withDeletedItems)
			this.deletedItems = new ConcurrentLinkedQueue<>();
	}

	@Override
	public long getKinah() {
		return kinahItem == null ? 0 : kinahItem.getItemCount();
	}

	@Override
	public Item getKinahItem() {
		return kinahItem;
	}

	@Override
	public StorageType getStorageType() {
		return storageType;
	}

	void increaseKinah(long amount, Player actor) {
		increaseKinah(amount, ItemUpdateType.INC_KINAH_COLLECT, actor);
	}

	void increaseKinah(long amount, ItemUpdateType updateType, Player actor) {
		if (kinahItem == null) {
			add(ItemFactory.newItem(ItemId.KINAH, 0), actor);
		}
		if (amount > 0) {
			increaseItemCount(kinahItem, amount, updateType, actor);
		}
	}

	/**
	 * Decrease kinah by {@code amount} but check first that its enough in storage
	 * 
	 * @return true if decrease was successful
	 */
	boolean tryDecreaseKinah(long amount, Player actor) {
		if (getKinah() >= amount) {
			decreaseKinah(amount, actor);
			return true;
		}
		return false;
	}

	boolean tryDecreaseKinah(long amount, ItemUpdateType updateType, Player actor) {
		if (getKinah() >= amount) {
			decreaseKinah(amount, updateType, actor);
			return true;
		}
		return false;
	}

	/**
	 * just decrease kinah without any checks
	 */
	void decreaseKinah(long amount, Player actor) {
		decreaseKinah(amount, ItemUpdateType.DEC_KINAH_BUY, actor);
	}

	void decreaseKinah(long amount, ItemUpdateType updateType, Player actor) {
		if (amount > 0) {
			decreaseItemCount(kinahItem, amount, updateType, actor);
		}
	}

	long increaseItemCount(Item item, long count, Player actor) {
		return increaseItemCount(item, count, ItemUpdateType.DEC_ITEM_USE, actor);
	}

	/**
	 * increase item count and return left count
	 */
	long increaseItemCount(Item item, long count, ItemUpdateType updateType, Player actor) {
		long leftCount = item.increaseItemCount(count);
		if (actor != null)
			ItemPacketService.sendItemPacket(actor, storageType, item, updateType);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return leftCount;
	}

	long decreaseItemCount(Item item, long count, Player actor) {
		return decreaseItemCount(item, count, ItemUpdateType.DEC_ITEM_USE, actor);
	}

	/**
	 * decrease item count and return left count
	 */
	long decreaseItemCount(Item item, long count, ItemUpdateType updateType, Player actor) {
		return decreaseItemCount(item, count, updateType, null, actor);
	}

	long decreaseItemCount(Item item, long count, ItemUpdateType updateType, QuestStatus questStatus, Player actor) {
		if (item == null)
			return 0;

		ItemDeleteType deleteType = questStatus != null ? ItemDeleteType.fromQuestStatus(questStatus) : ItemDeleteType.fromUpdateType(updateType);
		long leftCount = item.decreaseItemCount(count);
		boolean isKinah = item.getItemTemplate().isKinah();
		if (item.getItemCount() <= 0 && !isKinah)
			delete(item, deleteType, actor);
		else
			ItemPacketService.sendItemPacket(actor, storageType, item, updateType);

		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return leftCount;
	}

	/**
	 * This method should be called only for new items added to inventory (loading from DB) If item is equiped - will be put to equipment if item is
	 * unequiped - will be put to default bag for now Kinah is stored separately as it will be used frequently
	 * 
	 * @param item
	 */
	@Override
	public void onLoadHandler(Item item) {
		if (item.getItemTemplate().isKinah())
			kinahItem = item;
		else
			itemStorage.putItem(item);
	}

	Item add(Item item, Player actor) {
		return add(item, ItemService.DEFAULT_UPDATE_PREDICATE.getAddType(), actor);
	}

	Item add(Item item, ItemAddType addType, Player actor) {
		if (item.getItemTemplate().isKinah()) {
			this.kinahItem = item;
		} else if (!itemStorage.putItem(item)) {
			return null;
		}
		item.setItemLocation(storageType.getId());
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		if (actor != null) {
			ItemPacketService.sendStorageUpdatePacket(actor, storageType, item, addType);
			if (storageType == StorageType.CUBE)
				QuestEngine.getInstance().onItemGet(actor, item.getItemId());
		}
		return item;
	}

	/**
	 * used only for character transfers
	 * 
	 * @param item
	 * @param addType
	 * @param actor
	 * @return
	 */
	public Item add_CharacterTransfer(Item item) {
		if (item.getItemTemplate().isKinah()) {
			this.kinahItem = item;
		} else if (!itemStorage.putItem(item)) {
			return null;
		}
		item.setItemLocation(storageType.getId());
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return item;
	}

	// a bit misleading name - but looks like its used only for equipment
	Item put(Item item, Player actor) {
		if (!itemStorage.putItem(item)) {
			return null;
		}
		item.setItemLocation(storageType.getId());
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		ItemPacketService.sendItemUpdatePacket(actor, storageType, item, ItemUpdateType.EQUIP_UNEQUIP);
		return item;
	}

	/**
	 * Remove item from storage without changing its state
	 */
	@Override
	public Item remove(Item item) {
		return itemStorage.removeItem(item.getObjectId());
	}

	/**
	 * Delete item from storage and mark for DB update
	 */
	Item delete(Item item, Player actor) {
		return delete(item, ItemDeleteType.DEFAULT, actor);
	}

	/**
	 * Delete item from storage and mark for DB update
	 */
	Item delete(Item item, ItemDeleteType deleteType, Player actor) {
		if (remove(item) != null) {
			item.setPersistentState(PersistentState.DELETED);
			deletedItems.add(item);
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			ItemPacketService.sendItemDeletePacket(actor, StorageType.getStorageTypeById(item.getItemLocation()), item, deleteType);
			if (LoggingConfig.LOG_ITEM && !item.getItemTemplate().isKinah() && item.getItemCount() > 0) {
				String name = (item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() + " " : "") + item.getItemName();
				log.info("Deleted " +  item.getItemId() + " " + name + " from " + actor + " (count: " + item.getItemCount() + ") (deletion type: " + deleteType + ")");
			}
			QuestEngine.getInstance().onItemRemoved(actor, item.getItemId());
			return item;
		}
		return null;
	}

	boolean decreaseByItemId(int itemId, long count, Player actor) {
		return decreaseByItemId(itemId, count, null, actor);
	}

	boolean decreaseByItemId(int itemId, long count, QuestStatus questStatus, Player actor) {
		List<Item> items = itemStorage.getItemsById(itemId);
		if (items.size() == 0)
			return false;

		for (Item item : items) {
			if (count == 0) {
				break;
			}
			count = decreaseItemCount(item, count, ItemUpdateType.DEC_ITEM_USE, questStatus, actor);
		}

		return count == 0;
	}

	boolean decreaseByObjectId(int itemObjId, long count, Player actor) {
		return decreaseByObjectId(itemObjId, count, ItemUpdateType.DEC_ITEM_USE, actor);
	}

	boolean decreaseByObjectId(int itemObjId, long count, QuestStatus questStatus, Player actor) {
		Item item = itemStorage.getItemByObjId(itemObjId);
		if (item == null || item.getItemCount() < count)
			return false;

		return decreaseItemCount(item, count, ItemUpdateType.DEC_ITEM_USE, questStatus, actor) == 0;
	}

	boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType, Player actor) {
		Item item = itemStorage.getItemByObjId(itemObjId);
		if (item == null || item.getItemCount() < count)
			return false;

		return decreaseItemCount(item, count, updateType, actor) == 0;
	}

	@Override
	public Item getFirstItemByItemId(int itemId) {
		return this.itemStorage.getFirstItemById(itemId);
	}

	@Override
	public List<Item> getItemsWithKinah() {
		List<Item> items = this.itemStorage.getItems();
		if (this.kinahItem != null) {
			items.add(this.kinahItem);
		}
		return items;
	}

	@Override
	public List<Item> getItems() {
		return this.itemStorage.getItems();
	}

	@Override
	public List<Item> getItemsByItemId(int itemId) {
		return this.itemStorage.getItemsById(itemId);
	}

	@Override
	public Queue<Item> getDeletedItems() {
		return deletedItems;
	}

	@Override
	public Item getItemByObjId(int itemObjId) {
		return this.itemStorage.getItemByObjId(itemObjId);
	}

	@Override
	public long getItemCountByItemId(int itemId) {
		List<Item> temp = this.itemStorage.getItemsById(itemId);
		if (temp.size() == 0)
			return 0;

		long cnt = 0;
		for (Item item : temp)
			cnt += item.getItemCount();

		return cnt;
	}

	@Override
	public boolean isFull() {
		return this.itemStorage.isFull();
	}

	public boolean isFullSpecialCube() {
		return this.itemStorage.isFullSpecialCube();
	}

	public boolean isFull(int inventory) {
		if (inventory > 0) {
			return isFullSpecialCube();
		}
		return isFull();
	}

	public int getFreeSlots(int inventory) {
		if (inventory > 0) {
			return getSpecialCubeFreeSlots();
		}
		return getFreeSlots();
	}

	public int getSpecialCubeFreeSlots() {
		return this.itemStorage.getSpecialCubeFreeSlots();
	}

	@Override
	public int getFreeSlots() {
		return this.itemStorage.getFreeSlots();
	}

	public void setLimit(int limit) {
		itemStorage.setLimit(limit);
	}

	@Override
	public int getLimit() {
		return this.itemStorage.getLimit();
	}

	@Override
	public int getRowLength() {
		return this.itemStorage.getRowLength();
	}

	@Override
	public final PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public final void setPersistentState(PersistentState persistentState) {
		this.persistentState = persistentState;
	}

	@Override
	public int size() {
		return itemStorage.size();
	}
}
