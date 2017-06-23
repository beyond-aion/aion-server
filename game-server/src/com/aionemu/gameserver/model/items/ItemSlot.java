package com.aionemu.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum is defining inventory slots, to which items can be equipped.
 * 
 * @author Luno, xTz
 */
public enum ItemSlot {
	MAIN_HAND(1L),
	SUB_HAND(1L << 1),
	HELMET(1L << 2),
	TORSO(1L << 3),
	GLOVES(1L << 4),
	BOOTS(1L << 5),
	EARRINGS_LEFT(1L << 6),
	EARRINGS_RIGHT(1L << 7),
	RING_LEFT(1L << 8),
	RING_RIGHT(1L << 9),
	NECKLACE(1L << 10),
	SHOULDER(1L << 11),
	PANTS(1L << 12),
	POWER_SHARD_RIGHT(1L << 13),
	POWER_SHARD_LEFT(1L << 14),
	WINGS(1L << 15),
	WAIST(1L << 16),
	MAIN_OFF_HAND(1L << 17),
	SUB_OFF_HAND(1L << 18),
	PLUME(1L << 19),

	// combo
	MAIN_OR_SUB(MAIN_HAND.slotIdMask | SUB_HAND.slotIdMask, true), // 3
	MAIN_OFF_OR_SUB_OFF(MAIN_OFF_HAND.slotIdMask | SUB_OFF_HAND.slotIdMask, true),
	EARRING_RIGHT_OR_LEFT(EARRINGS_LEFT.slotIdMask | EARRINGS_RIGHT.slotIdMask, true), // 192
	RING_RIGHT_OR_LEFT(RING_LEFT.slotIdMask | RING_RIGHT.slotIdMask, true), // 768
	SHARD_RIGHT_OR_LEFT(POWER_SHARD_LEFT.slotIdMask | POWER_SHARD_RIGHT.slotIdMask, true), // 24576
	RIGHT_HAND(MAIN_HAND.slotIdMask | MAIN_OFF_HAND.slotIdMask, true),
	LEFT_HAND(SUB_HAND.slotIdMask | SUB_OFF_HAND.slotIdMask, true),
	VISIBLE(MAIN_HAND.slotIdMask
					| SUB_HAND.slotIdMask
					| HELMET.slotIdMask
					| TORSO.slotIdMask
					| GLOVES.slotIdMask
					| BOOTS.slotIdMask
					| EARRINGS_LEFT.slotIdMask
					| EARRINGS_RIGHT.slotIdMask
					| NECKLACE.slotIdMask
					| SHOULDER.slotIdMask
					| PANTS.slotIdMask
					| POWER_SHARD_RIGHT.slotIdMask
					| POWER_SHARD_LEFT.slotIdMask
					| WINGS.slotIdMask
					| PLUME.slotIdMask,
					true), // rings were designed to be visible (at the players thumbs), but they have no skins

	// STIGMA slots
	STIGMA1(1L << 30),
	STIGMA2(1L << 31),
	STIGMA3(1L << 32),

	REGULAR_STIGMAS(STIGMA1.slotIdMask | STIGMA2.slotIdMask | STIGMA3.slotIdMask, true),
	ADV_STIGMA1(1L << 33),
	ADV_STIGMA2(1L << 34),
	ADV_STIGMA3(1L << 35),

	ADVANCED_STIGMAS(ADV_STIGMA1.slotIdMask | ADV_STIGMA2.slotIdMask | ADV_STIGMA3.slotIdMask, true),

	ALL_STIGMA(REGULAR_STIGMAS.slotIdMask | ADVANCED_STIGMAS.slotIdMask, true);

	private long slotIdMask;
	private boolean combo;

	private ItemSlot(long mask) {
		this(mask, false);
	}

	private ItemSlot(long mask, boolean combo) {
		if (mask == 0)
			throw new InstantiationError("ItemSlot mask cannot be 0");
		this.slotIdMask = mask;
		this.combo = combo;
	}

	public long getSlotIdMask() {
		return slotIdMask;
	}

	/**
	 * @return the combo
	 */
	public boolean isCombo() {
		return combo;
	}

	public static boolean isAdvancedStigma(long slot) {
		return (ADVANCED_STIGMAS.slotIdMask & slot) == slot;
	}

	public static boolean isRegularStigma(long slot) {
		return (REGULAR_STIGMAS.slotIdMask & slot) == slot;
	}

	public static boolean isStigma(long slot) {
		return (ALL_STIGMA.slotIdMask & slot) == slot;
	}

	public static boolean isVisible(long slot) {
		return (VISIBLE.slotIdMask & slot) == slot;
	}

	public static boolean isTwoHandedWeapon(long slot) {
		return (slot & MAIN_OR_SUB.slotIdMask) == MAIN_OR_SUB.slotIdMask || (slot & MAIN_OFF_OR_SUB_OFF.slotIdMask) == MAIN_OFF_OR_SUB_OFF.slotIdMask;
	}

	public static byte getEquipmentSlotType(long slot) {
		if (!isVisible(slot))
			return 0; // not equippable

		long leftSlotMask = SUB_HAND.slotIdMask | EARRINGS_LEFT.slotIdMask | RING_LEFT.slotIdMask | POWER_SHARD_LEFT.slotIdMask | SUB_OFF_HAND.slotIdMask;
		if ((slot & leftSlotMask) == 0 || isTwoHandedWeapon(slot))
			return 1; // default (right-hand) slot

		return 2; // secondary (left-hand) slot
	}

	public static ItemSlot[] getSlotsFor(long slotIdMask) {
		if (slotIdMask == 0)
			throw new IllegalArgumentException("slotIdMask cannot be 0");
		List<ItemSlot> slots = new ArrayList<>();
		for (ItemSlot itemSlot : values()) {
			if (!itemSlot.isCombo() && (slotIdMask & itemSlot.slotIdMask) == itemSlot.slotIdMask)
				slots.add(itemSlot);
		}
		return slots.toArray(new ItemSlot[slots.size()]);
	}

	public static ItemSlot getSlotFor(long slot) {
		return getSlotsFor(slot)[0];
	}
}
