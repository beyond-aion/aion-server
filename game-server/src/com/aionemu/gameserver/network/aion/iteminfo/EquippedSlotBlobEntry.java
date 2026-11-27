package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * This block is sent for all items that can be equipped. If item is equipped. This block says to which slot it's equipped. If not, then it says 0.
 * 
 * @author -Nemesiss-, Rolandas
 */
public class EquippedSlotBlobEntry extends ItemBlobEntry {

	EquippedSlotBlobEntry() {
		super(ItemBlobType.EQUIPPED_SLOT);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeQ(buf, ownerItem.isEquipped() ? ownerItem.getEquipmentSlot() : 0);
	}

	@Override
	public int getSize() {
		return 8;
	}
}
