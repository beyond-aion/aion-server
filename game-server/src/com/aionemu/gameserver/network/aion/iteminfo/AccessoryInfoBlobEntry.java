package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob is sent for accessory items (such as ring, earring, waist). It keeps info about slots that item can be equipped to.
 * 
 * @author -Nemesiss-, Rolandas
 */
public class AccessoryInfoBlobEntry extends ItemBlobEntry {

	AccessoryInfoBlobEntry() {
		super(ItemBlobType.SLOTS_ACCESSORY);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		ItemSlot[] slots = ItemSlot.getSlotsFor(ownerItem.getItemTemplate().getItemSlot());
		writeQ(buf, slots[0].getSlotIdMask());
		writeQ(buf, slots.length > 1 ? slots[1].getSlotIdMask() : 0);
	}

	@Override
	public int getSize() {
		return 16;
	}
}
