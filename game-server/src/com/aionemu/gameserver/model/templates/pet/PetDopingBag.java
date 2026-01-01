package com.aionemu.gameserver.model.templates.pet;

import java.util.Arrays;

/**
 * @author Rolandas
 */
public class PetDopingBag {

	public static final int MAX_ITEMS = 8; // food slot, drink slot and 6 scroll slots
	private int[] itemBag = null;
	private boolean isDirty = false;

	public void setFoodItem(int itemId) {
		setItem(itemId, 0);
	}

	public int getFoodItem() {
		if (itemBag == null || itemBag.length < 1)
			return 0;
		return itemBag[0];
	}

	public void setDrinkItem(int itemId) {
		setItem(itemId, 1);
	}

	public int getDrinkItem() {
		if (itemBag == null || itemBag.length < 2)
			return 0;
		return itemBag[1];
	}

	/**
	 * Adds or removes item to the bag
	 * 
	 * @param itemId
	 *          - item Id, or 0 to remove
	 * @param slot
	 *          - slot number; 0 for food, 1 for drink, the rest are for scrolls
	 */
	public synchronized void setItem(int itemId, int slot) {
		if (slot < 0 || slot >= MAX_ITEMS)
			throw new IllegalArgumentException("Slot index " + slot + " for item " + itemId + " is invalid.");
		if (itemBag == null || slot >= itemBag.length)
			itemBag = itemBag == null ? new int[slot + 1] : Arrays.copyOf(itemBag, slot + 1);
		if (itemBag[slot] != itemId) {
			itemBag[slot] = itemId;
			isDirty = true;
		}
	}

	public int[] getScrollsUsed() {
		if (itemBag == null || itemBag.length < 3)
			return new int[0];
		return Arrays.copyOfRange(itemBag, 2, itemBag.length);
	}

	public int[] getItems() {
		return itemBag == null ? new int[0] : itemBag;
	}

	/**
	 * Currently only scrolls can be relocated
	 */
	public void switchItems(int slot1, int slot2) {
		if (slot1 < 2 || slot2 < 2)
			return;
		int slot1Item = itemBag.length > slot1 ? itemBag[slot1] : 0;
		int slot2Item = itemBag.length > slot2 ? itemBag[slot2] : 0;
		setItem(slot1Item, slot2);
		setItem(slot2Item, slot1);
	}

	/**
	 * @return true if the bag needs saving
	 */
	public boolean isDirty() {
		return isDirty;
	}

}
