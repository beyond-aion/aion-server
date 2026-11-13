package com.aionemu.gameserver.model.items.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
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
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

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
	public synchronized long getKinah() {
		return kinahItem == null ? 0 : kinahItem.getItemCount();
	}

	@Override
	public synchronized Item getKinahItem() {
		return kinahItem;
	}

	@Override
	public StorageType getStorageType() {
		return storageType;
	}

	/*
	 * Other players' threads change this storage too (trade partners, loot winners, private store buyers), so its state changes under this
	 * instance's monitor, one operation at a time. Packets, logs and quest events follow after the monitor was released: they may reach other
	 * storages, whose monitors must never be taken while holding this one.
	 */
	void increaseKinah(long amount, Player actor) {
		increaseKinah(amount, ItemUpdateType.INC_KINAH_COLLECT, actor);
	}

	void increaseKinah(long amount, ItemUpdateType updateType, Player actor) {
		Item createdKinahItem = null;
		Item kinah;
		synchronized (this) {
			if (kinahItem == null) {
				createdKinahItem = ItemFactory.newItem(ItemId.KINAH, 0);
				putLocked(createdKinahItem);
			}
			kinah = kinahItem;
			if (amount > 0)
				increaseItemCountLocked(kinah, amount);
		}
		if (createdKinahItem != null)
			onAdded(createdKinahItem, ItemService.DEFAULT_UPDATE_PREDICATE.getAddType(), actor);
		if (amount > 0 && actor != null)
			ItemPacketService.sendItemPacket(actor, storageType, kinah, updateType);
	}

	/**
	 * Decrease kinah by {@code amount} but check first that its enough in storage
	 * 
	 * @return true if decrease was successful
	 */
	boolean tryDecreaseKinah(long amount, Player actor) {
		return tryDecreaseKinah(amount, ItemUpdateType.DEC_KINAH_BUY, actor);
	}

	boolean tryDecreaseKinah(long amount, ItemUpdateType updateType, Player actor) {
		Item kinah;
		synchronized (this) {
			if (getKinah() < amount)
				return false;
			kinah = kinahItem;
			if (amount > 0)
				decreaseItemCountLocked(kinah, amount);
		}
		if (amount > 0)
			ItemPacketService.sendItemPacket(actor, storageType, kinah, updateType);
		return true;
	}

	/**
	 * just decrease kinah without any checks
	 */
	void decreaseKinah(long amount, Player actor) {
		decreaseKinah(amount, ItemUpdateType.DEC_KINAH_BUY, actor);
	}

	void decreaseKinah(long amount, ItemUpdateType updateType, Player actor) {
		if (amount > 0)
			decreaseItemCount(getKinahItem(), amount, updateType, actor);
	}

	long increaseItemCount(Item item, long count, Player actor) {
		return increaseItemCount(item, count, ItemUpdateType.DEC_ITEM_USE, actor);
	}

	/**
	 * increase item count and return left count
	 */
	long increaseItemCount(Item item, long count, ItemUpdateType updateType, Player actor) {
		long leftCount;
		synchronized (this) {
			leftCount = increaseItemCountLocked(item, count);
		}
		if (actor != null)
			ItemPacketService.sendItemPacket(actor, storageType, item, updateType);
		return leftCount;
	}

	private long increaseItemCountLocked(Item item, long count) {
		long leftCount = item.increaseItemCount(count);
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
		long leftCount;
		boolean used, deleted = false;
		synchronized (this) {
			leftCount = decreaseItemCountLocked(item, count);
			used = item.getItemCount() <= 0 && !item.getItemTemplate().isKinah();
			if (used)
				deleted = deleteLocked(item);
		}
		if (deleted)
			onDeleted(item, deleteType, actor);
		else if (!used)
			ItemPacketService.sendItemPacket(actor, storageType, item, updateType);
		return leftCount;
	}

	private long decreaseItemCountLocked(Item item, long count) {
		long leftCount = item.decreaseItemCount(count);
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
	public synchronized void onLoadHandler(Item item) {
		if (item.getItemTemplate().isKinah())
			kinahItem = item;
		else
			itemStorage.putItem(item);
	}

	Item add(Item item, Player actor) {
		return add(item, ItemService.DEFAULT_UPDATE_PREDICATE.getAddType(), actor);
	}

	Item add(Item item, ItemAddType addType, Player actor) {
		synchronized (this) {
			if (!putLocked(item))
				return null;
		}
		onAdded(item, addType, actor);
		return item;
	}

	private boolean putLocked(Item item) {
		if (item.getItemTemplate().isKinah()) {
			this.kinahItem = item;
		} else if (!itemStorage.putItem(item)) {
			return false;
		}
		item.setItemLocation(storageType.getId());
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return true;
	}

	private void onAdded(Item item, ItemAddType addType, Player actor) {
		if (actor != null) {
			ItemPacketService.sendStorageUpdatePacket(actor, storageType, item, addType);
			if (storageType == StorageType.CUBE)
				QuestEngine.getInstance().onItemGet(actor, item.getItemId());
		}
	}

	/**
	 * used only for character transfers
	 * 
	 * @param item
	 * @param addType
	 * @param actor
	 * @return
	 */
	public synchronized Item add_CharacterTransfer(Item item) {
		return putLocked(item) ? item : null;
	}

	// a bit misleading name - but looks like its used only for equipment
	Item put(Item item, Player actor) {
		synchronized (this) {
			if (!itemStorage.putItem(item))
				return null;
			item.setItemLocation(storageType.getId());
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
		ItemPacketService.sendItemUpdatePacket(actor, storageType, item, ItemUpdateType.EQUIP_UNEQUIP);
		return item;
	}

	/**
	 * Remove item from storage without changing its state
	 */
	@Override
	public synchronized Item remove(Item item) {
		return itemStorage.getItemByObjId(item.getObjectId()) == item ? itemStorage.removeItem(item.getObjectId()) : null;
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
		boolean deleted;
		synchronized (this) {
			deleted = deleteLocked(item);
		}
		if (!deleted)
			return null;
		onDeleted(item, deleteType, actor);
		return item;
	}

	private boolean deleteLocked(Item item) {
		if (remove(item) == null)
			return false;
		item.setPersistentState(PersistentState.DELETED);
		deletedItems.add(item);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return true;
	}

	private void onDeleted(Item item, ItemDeleteType deleteType, Player actor) {
		ItemPacketService.sendItemDeletePacket(actor, StorageType.getStorageTypeById(item.getItemLocation()), item, deleteType);
		if (LoggingConfig.LOG_ITEM && !item.getItemTemplate().isKinah() && item.getItemCount() > 0) {
			String name = (item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() + " " : "") + item.getItemName();
			log.info("Deleted {} {} from {} (count: {}) (deletion type: {})", item.getItemId(), name, actor, item.getItemCount(), deleteType);
		}
		QuestEngine.getInstance().onItemRemoved(actor, item.getItemId());
	}

	private void onMovedOut(Item item, Player actor, ItemDeleteType deleteType) {
		ItemPacketService.sendItemDeletePacket(actor, storageType, item, deleteType);
		if (LoggingConfig.LOG_ITEM && !item.getItemTemplate().isKinah() && item.getItemCount() > 0) {
			String name = (item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() + " " : "") + item.getItemName();
			log.info("Moved {} {} out of {} (count: {}) ({}), keeping its database row", item.getItemId(), name, actor, item.getItemCount(),
				deleteType);
		}
		QuestEngine.getInstance().onItemRemoved(actor, item.getItemId());
	}

	/**
	 * Moves a sale out of the cube. The caller holds the player's item save lock through publication of the buy-back list.
	 */
	public Item sellForRepurchase(Player seller, Item item, long count, long reward) {
		Item sold;
		boolean wholeStack;
		Item previousKinah;
		long oldBalance;
		synchronized (this) {
			long oldCount = item.getItemCount();
			if (count <= 0 || reward < 0 || getItemByObjId(item.getObjectId()) != item || count > oldCount)
				return null;

			wholeStack = count == oldCount;
			if (wholeStack) {
				sold = remove(item);
			} else {
				sold = ItemFactory.newItem(item.getItemId(), count);
				sold.setExpireTime(item.getExpireTime());
				item.setItemCount(oldCount - count);
			}
			sold.setItemLocation(StorageType.REPURCHASE.getId());
			previousKinah = kinahItem;
			oldBalance = getKinah();
			creditKinahLocked(reward);
			List<Item> changedItems = new ArrayList<>(3);
			changedItems.add(sold);
			if (!wholeStack)
				changedItems.add(item);
			if (kinahItem != null)
				changedItems.add(kinahItem);
			if (!InventoryDAO.storeAtomically(changedItems, seller.getObjectId())) {
				restoreKinahLocked(previousKinah, oldBalance);
				if (wholeStack)
					putLocked(sold);
				else {
					item.setItemCount(oldCount);
					IDFactory.getInstance().releaseId(sold.getObjectId());
				}
				return null;
			}
			sold.setRepurchasePrice(reward);
			if (wholeStack)
				ExpireTimerTask.getInstance().unregisterExpirable(sold, seller);
		}
		if (wholeStack)
			onMovedOut(sold, seller, ItemDeleteType.SELL);
		else
			ItemPacketService.sendItemPacket(seller, storageType, item, ItemUpdateType.DEC_ITEM_USE);
		notifyKinahChange(seller, previousKinah, oldBalance, ItemUpdateType.INC_KINAH_SELL);
		return sold;
	}

	/**
	 * Takes payment and moves the item back to the cube under the caller's item save lock.
	 */
	public boolean repurchase(Player buyer, Item item) {
		long price = item.getRepurchasePrice();
		long oldCount = item.getItemCount();
		Map<Item, Long> changedStacks = new LinkedHashMap<>();
		long oldBalance;
		boolean merged;
		synchronized (this) {
			oldBalance = getKinah();
			if (price < 0 || oldBalance < price || item.getItemLocation() != StorageType.REPURCHASE.getId()
				|| getItemByObjId(item.getObjectId()) != null || isFull(item.getItemTemplate().getExtraInventoryId()))
				return false;

			long remaining = item.getItemTemplate().isStackable() ? mergeStoreStackLocked(item, oldCount, changedStacks) : oldCount;
			merged = remaining == 0;
			long oldSlot = item.getEquipmentSlot();
			if (merged)
				item.setPersistentState(PersistentState.DELETED);
			else {
				item.setItemCount(remaining);
				item.setItemLocation(storageType.getId());
				item.setEquipmentSlot(ItemStorage.FIRST_AVAILABLE_SLOT);
			}
			List<Item> changedItems = new ArrayList<>(changedStacks.keySet());
			changedItems.add(item);
			Item payment = kinahItem;
			if (price > 0) {
				payment.setItemCount(oldBalance - price);
				changedItems.add(payment);
			}
			if (!InventoryDAO.storeAtomically(changedItems, buyer.getObjectId())) {
				restoreKinahLocked(payment, oldBalance);
				changedStacks.forEach(Item::setItemCount);
				item.setItemCount(oldCount);
				item.setItemLocation(StorageType.REPURCHASE.getId());
				item.setEquipmentSlot(oldSlot);
				return false;
			}
			if (!merged) {
				itemStorage.putItem(item);
				ExpireTimerTask.getInstance().registerExpirable(item, buyer);
			}
		}
		notifyStoreStacks(buyer, changedStacks.keySet());
		if (!merged)
			onAdded(item, ItemAddType.ITEM_COLLECT, buyer);
		if (price > 0)
			ItemPacketService.sendItemPacket(buyer, storageType, getKinahItem(), ItemUpdateType.DEC_KINAH_BUY);
		return true;
	}

	public static boolean sellPrivateStoreItem(Player seller, Player buyer, Item item, long count, long price) {
		Player first = seller.getObjectId() < buyer.getObjectId() ? seller : buyer;
		Player second = first == seller ? buyer : seller;
		Storage source = seller.getInventory();
		Storage destination = buyer.getInventory();
		PrivateSale sale;
		synchronized (first.getItemSaveLock()) {
			synchronized (second.getItemSaveLock()) {
				synchronized (first.getInventory()) {
					synchronized (second.getInventory()) {
						sale = source.storePrivateSale(seller, buyer, item, count, price);
					}
				}
			}
		}
		if (sale == null)
			return false;
		if (sale.removed())
			source.onMovedOut(item, seller, ItemDeleteType.USE);
		else
			ItemPacketService.sendItemPacket(seller, source.storageType, item, ItemUpdateType.DEC_ITEM_USE);
		destination.notifyStoreStacks(buyer, sale.updatedStacks());
		if (sale.delivered() != null)
			destination.onAdded(sale.delivered(), ItemAddType.ITEM_COLLECT, buyer);
		source.notifyKinahChange(seller, sale.previousKinah(), sale.oldBalance(), ItemUpdateType.INC_KINAH_COLLECT);
		if (price > 0)
			ItemPacketService.sendItemPacket(buyer, destination.storageType, destination.getKinahItem(), ItemUpdateType.DEC_KINAH_BUY);
		return true;
	}

	private record PrivateSale(Item delivered, List<Item> updatedStacks, boolean removed, Item previousKinah, long oldBalance) {
	}

	private PrivateSale storePrivateSale(Player seller, Player buyer, Item item, long count, long price) {
		Storage destination = buyer.getInventory();
		long buyerBalance = destination.getKinah();
		long oldCount = item.getItemCount();
		if (count <= 0 || price < 0 || buyerBalance < price || destination.isFull(item.getItemTemplate().getExtraInventoryId())
			|| destination.getItemByObjId(item.getObjectId()) != null || getItemByObjId(item.getObjectId()) != item || count > oldCount)
			return null;

		boolean stackSale = item.getItemTemplate().isStackable();
		boolean removed = oldCount == count;
		Map<Item, Long> changedStacks = new LinkedHashMap<>();
		Item delivered = item;
		PersistentState oldState = item.getPersistentState();
		long oldSlot = item.getEquipmentSlot();
		int oldPackCount = item.getPackCount();
		if (!stackSale && !removed)
			return null;
		List<Item> sellerItems = new ArrayList<>(2);
		if (removed)
			remove(item);
		if (stackSale) {
			long remaining = destination.mergeStoreStackLocked(item, count, changedStacks);
			delivered = remaining == 0 ? null : ItemFactory.newItem(item.getItemId(), remaining);
			if (delivered != null)
				delivered.setExpireTime(item.getExpireTime());
			if (removed)
				item.setPersistentState(PersistentState.DELETED);
			else
				item.setItemCount(oldCount - count);
			sellerItems.add(item);
		}
		if (oldPackCount > 0)
			item.setPackCount(oldPackCount - 1);
		List<Item> buyerItems = new ArrayList<>(changedStacks.keySet());
		if (delivered != null) {
			delivered.setItemLocation(destination.storageType.getId());
			delivered.setEquipmentSlot(ItemStorage.FIRST_AVAILABLE_SLOT);
			buyerItems.add(delivered);
		}
		Item previousKinah = kinahItem;
		long sellerBalance = getKinah();
		Item payment = destination.kinahItem;
		if (price > 0) {
			creditKinahLocked(price);
			sellerItems.add(kinahItem);
			payment.setItemCount(buyerBalance - price);
			buyerItems.add(payment);
		}
		if (!InventoryDAO.storePrivateStoreSale(buyerItems, buyer.getObjectId(), sellerItems, seller.getObjectId())) {
			restoreKinahLocked(previousKinah, sellerBalance);
			destination.restoreKinahLocked(payment, buyerBalance);
			item.setPackCount(oldPackCount);
			if (stackSale) {
				if (removed) {
					item.setPersistentState(oldState);
					itemStorage.putItem(item);
				} else
					item.setItemCount(oldCount);
				changedStacks.forEach(Item::setItemCount);
				if (delivered != null)
					IDFactory.getInstance().releaseId(delivered.getObjectId());
			} else {
				item.setEquipmentSlot(oldSlot);
				putLocked(item);
			}
			return null;
		}
		if (stackSale && removed) {
			ExpireTimerTask.getInstance().unregisterExpirable(item, seller);
			if (oldState == PersistentState.NEW)
				IDFactory.getInstance().releaseId(item.getObjectId());
		}
		if (delivered != null) {
			destination.itemStorage.putItem(delivered);
			ExpireTimerTask.getInstance().registerExpirable(delivered, buyer);
		}
		return new PrivateSale(delivered, List.copyOf(changedStacks.keySet()), removed, previousKinah, sellerBalance);
	}

	private long mergeStoreStackLocked(Item item, long count, Map<Item, Long> changedStacks) {
		// Keep the purchase in the cube so combat cannot spend tentative charges from an equipped stack during the transaction.
		for (Item target : itemStorage.getItemsById(item.getItemId())) {
			long previous = target.getItemCount();
			count = target.increaseItemCount(count);
			if (target.getItemCount() != previous)
				changedStacks.put(target, previous);
			if (count == 0)
				break;
		}
		return count;
	}

	private void notifyStoreStacks(Player buyer, Iterable<Item> stacks) {
		for (Item stack : stacks)
			ItemPacketService.sendItemPacket(buyer, storageType, stack, ItemUpdateType.INC_ITEM_COLLECT);
	}

	private void creditKinahLocked(long amount) {
		if (amount <= 0)
			return;
		if (kinahItem == null)
			putLocked(ItemFactory.newItem(ItemId.KINAH, 0));
		long balance = kinahItem.getItemCount();
		long available = Math.max(0, kinahItem.getItemTemplate().getMaxStackCount() - balance);
		if (available > 0)
			kinahItem.setItemCount(balance + Math.min(amount, available));
	}

	private void restoreKinahLocked(Item previousKinah, long previousBalance) {
		if (previousKinah == null && kinahItem != null) {
			IDFactory.getInstance().releaseId(kinahItem.getObjectId());
			kinahItem = null;
		} else if (previousKinah != null && previousKinah.getItemCount() != previousBalance)
			previousKinah.setItemCount(previousBalance);
	}

	private void notifyKinahChange(Player player, Item previousKinah, long previousBalance, ItemUpdateType updateType) {
		Item kinah = getKinahItem();
		if (kinah == null || kinah.getItemCount() == previousBalance)
			return;
		if (previousKinah == null)
			onAdded(kinah, ItemAddType.ITEM_COLLECT, player);
		ItemPacketService.sendItemPacket(player, storageType, kinah, updateType);
	}

	boolean decreaseByItemId(int itemId, long count, Player actor) {
		return decreaseByItemId(itemId, count, null, actor);
	}

	boolean decreaseByItemId(int itemId, long count, QuestStatus questStatus, Player actor) {
		ItemDeleteType deleteType = questStatus != null ? ItemDeleteType.fromQuestStatus(questStatus) : ItemDeleteType.USE;
		List<Item> changedItems = new ArrayList<>();
		List<Item> removedItems = new ArrayList<>();
		synchronized (this) { // all stacks at once, so another thread can't take from them in between
			List<Item> items = itemStorage.getItemsById(itemId);
			if (items.isEmpty())
				return false;
			for (Item item : items) {
				if (count == 0)
					break;
				count = decreaseItemCountLocked(item, count);
				if (item.getItemCount() > 0 || item.getItemTemplate().isKinah())
					changedItems.add(item);
				else if (deleteLocked(item))
					removedItems.add(item);
			}
		}
		changedItems.forEach(item -> ItemPacketService.sendItemPacket(actor, storageType, item, ItemUpdateType.DEC_ITEM_USE));
		removedItems.forEach(item -> onDeleted(item, deleteType, actor));
		return count == 0;
	}

	boolean decreaseByObjectId(int itemObjId, long count, Player actor) {
		return decreaseByObjectId(itemObjId, count, ItemUpdateType.DEC_ITEM_USE, actor);
	}

	boolean decreaseByObjectId(int itemObjId, long count, QuestStatus questStatus, Player actor) {
		return decreaseByObjectId(itemObjId, count, ItemUpdateType.DEC_ITEM_USE, questStatus, actor);
	}

	boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType, Player actor) {
		return decreaseByObjectId(itemObjId, count, updateType, null, actor);
	}

	private boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType, QuestStatus questStatus, Player actor) {
		ItemDeleteType deleteType = questStatus != null ? ItemDeleteType.fromQuestStatus(questStatus) : ItemDeleteType.fromUpdateType(updateType);
		Item item;
		boolean used, deleted = false;
		synchronized (this) { // the check and the decrease at once, or two threads could both take the last of the stack
			item = itemStorage.getItemByObjId(itemObjId);
			if (item == null || item.getItemCount() < count)
				return false;
			decreaseItemCountLocked(item, count);
			used = item.getItemCount() <= 0 && !item.getItemTemplate().isKinah();
			if (used)
				deleted = deleteLocked(item);
		}
		if (deleted)
			onDeleted(item, deleteType, actor);
		else if (!used)
			ItemPacketService.sendItemPacket(actor, storageType, item, updateType);
		return true;
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
