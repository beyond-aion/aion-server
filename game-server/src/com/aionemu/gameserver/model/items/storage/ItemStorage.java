package com.aionemu.gameserver.model.items.storage;

import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Item;

import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * @author KID
 */
public class ItemStorage {

	public static final long FIRST_AVAILABLE_SLOT = 65535L;

	private FastMap<Integer, Item> items;
	private int limit;
	private int specialLimit;
	private final StorageType storageType;

	public ItemStorage(StorageType storageType) {
		this.limit = storageType.getLimit();
		this.specialLimit = storageType.getSpecialLimit();
		this.storageType = storageType;
		this.items = new FastMap<>();
	}

	public FastTable<Item> getItems() {
		FastTable<Item> temp = new FastTable<>();
		temp.addAll(items.values());
		return temp;
	}

	public int getLimit() {
		return this.limit;
	}

	public boolean setLimit(int limit) {
		if (getCubeItems().size() > limit) {
			return false;
		}

		this.limit = limit;
		return true;
	}

	public int getRowLength() {
		return storageType.getLength();
	}

	public Item getFirstItemById(int itemId) {
		for (Item item : items.values()) {
			if (item.getItemTemplate().getTemplateId() == itemId) {
				return item;
			}
		}
		return null;
	}

	public FastTable<Item> getItemsById(int itemId) {
		FastTable<Item> temp = new FastTable<>();
		for (Item item : items.values()) {
			if (item.getItemTemplate().getTemplateId() == itemId) {
				temp.add(item);
			}
		}
		return temp;
	}

	public Item getItemByObjId(int itemObjId) {
		return this.items.get(itemObjId);
	}

	public long getSlotIdByItemId(int itemId) {
		for (Item item : this.items.values()) {
			if (item.getItemTemplate().getTemplateId() == itemId) {
				return item.getEquipmentSlot();
			}
		}
		return -1;
	}

	public Item getItemBySlotId(short slotId) {
		for (Item item : getCubeItems()) {
			if (item.getEquipmentSlot() == slotId) {
				return item;
			}
		}
		return null;
	}

	public Item getSpecialItemBySlotId(short slotId) {
		for (Item item : getSpecialCubeItems()) {
			if (item.getEquipmentSlot() == slotId) {
				return item;
			}
		}
		return null;
	}

	public long getSlotIdByObjId(int objId) {
		Item item = this.getItemByObjId(objId);
		if (item != null)
			return item.getEquipmentSlot();
		else
			return -1;
	}

	public long getNextAvailableSlot() {
		return FIRST_AVAILABLE_SLOT;
	}

	public boolean putItem(Item item) {
		if (this.items.containsKey(item.getObjectId()))
			return false;

		this.items.put(item.getObjectId(), item);
		return true;
	}

	public Item removeItem(int objId) {
		return this.items.remove(objId);
	}

	public boolean isFull() {
		return getCubeItems().size() >= limit;
	}

	public boolean isFullSpecialCube() {
		return getSpecialCubeItems().size() >= specialLimit;
	}

	public List<Item> getSpecialCubeItems() {
		return items.values().stream().filter(i -> i.getItemTemplate().getExtraInventoryId() > 0).collect(Collectors.toList());
	}

	public List<Item> getCubeItems() {
		return items.values().stream().filter(i -> i.getItemTemplate().getExtraInventoryId() < 1).collect(Collectors.toList());
	}

	public int getFreeSlots() {
		return limit - getCubeItems().size();
	}

	public int getSpecialCubeFreeSlots() {
		return specialLimit - getSpecialCubeItems().size();
	}

	public int size() {
		return this.items.size();
	}

}
