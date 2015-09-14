package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This block is sent for all items that can be equipped. If item is equipped. This block says to which slot it's equipped. If not, then it says 0.
 * 
 * @author -Nemesiss-
 * @modified Rolandas
 */
public class EquippedSlotBlobEntry extends ItemBlobEntry {

	EquippedSlotBlobEntry() {
		super(ItemBlobType.EQUIPPED_SLOT);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Item item = ownerItem;

		if (item.isEquipped())
			writeQ(buf, item.getEquipmentSlot());
		else
			writeQ(buf, 0);
	}

	@Override
	public int getSize() {
		return 8;
	}
}
