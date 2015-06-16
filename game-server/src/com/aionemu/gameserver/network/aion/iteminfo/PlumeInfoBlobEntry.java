package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * @author Rolandas
 */
public class PlumeInfoBlobEntry extends ItemBlobEntry {

	PlumeInfoBlobEntry() {
		super(ItemBlobType.PLUME_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Item item = ownerItem;

		writeQ(buf, ItemSlot.getSlotFor(item.getItemTemplate().getItemSlot()).getSlotIdMask());
		writeQ(buf, 0x100000); // secondary slot ?
		writeD(buf, 0); // unks
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
	}

	public int getSize() {
		return 32;
	}

}
