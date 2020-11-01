package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob is sent for weapons. It keeps info about slots that weapon can be equipped to.
 * 
 * @author -Nemesiss-, Rolandas
 */
public class WeaponInfoBlobEntry extends ItemBlobEntry {

	WeaponInfoBlobEntry() {
		super(ItemBlobType.SLOTS_WEAPON);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		ItemSlot[] slots = ItemSlot.getSlotsFor(ownerItem.getItemTemplate().getItemSlot());
		if (slots.length == 1) {
			writeQ(buf, slots[0].getSlotIdMask());
			writeQ(buf, ownerItem.hasFusionedItem() ? 0x00 : 0x02);
			return;
		}
		if (ownerItem.getItemTemplate().isTwoHandWeapon()) {
			// must occupy two slots
			writeQ(buf, slots[0].getSlotIdMask() | slots[1].getSlotIdMask());
			writeQ(buf, 0);
		} else {
			// primary and secondary slots
			writeQ(buf, slots[0].getSlotIdMask());
			writeQ(buf, slots[1].getSlotIdMask());
		}
	}

	@Override
	public int getSize() {
		return 16;
	}
}
