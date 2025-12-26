package com.aionemu.gameserver.model.templates.pet;

import java.util.Arrays;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

public class PetDopingBag {
	public static final int MAX_ITEMS = 8;
	private int[] itemBag = null;
	private boolean isDirty = false;

	@SuppressWarnings("unused")
	public void setFoodItem(int itemId) {
		setItem(itemId, 0);
	}

	public int getFoodItem() {
		if (itemBag == null || itemBag.length < 1) {
			return 0;
		}
		return itemBag[0];
	}

	@SuppressWarnings("unused")
	public void setDrinkItem(int itemId) {
		setItem(itemId, 1);
	}

	public int getDrinkItem() {
		if (itemBag == null || itemBag.length < 2) {
			return 0;
		}
		return itemBag[1];
	}

	public synchronized void setItem(int itemId, int slot) {
		if (slot < 0 || slot >= MAX_ITEMS) {
			throw new IllegalArgumentException("Slot index " + slot + " for item " + itemId + " is invalid.");
		}
		if (itemBag == null || slot >= itemBag.length) {
			itemBag = itemBag == null ? new int[slot + 1] : Arrays.copyOf(itemBag, slot + 1);
		}
		if (itemBag[slot] != itemId) {
			itemBag[slot] = itemId;
			isDirty = true;
		}
	}

	public int[] getScrollsUsed() {
		if (itemBag == null || itemBag.length < 3) {
			return new int[0];
		}
		return Arrays.copyOfRange(itemBag, 2, itemBag.length);
	}

	public int[] getItems() {
		return itemBag == null ? new int[0] : itemBag;
	}

	public void switchItems(int slot1, int slot2) {
		if (slot1 < 2 || slot2 < 2) {
			return;
		}
		int slot1Item = itemBag.length > slot1 ? itemBag[slot1] : 0;
		int slot2Item = itemBag.length > slot2 ? itemBag[slot2] : 0;
		setItem(slot1Item, slot2);
		setItem(slot2Item, slot1);
	}

	public boolean isDirty() {
		return this.isDirty;
	}
}