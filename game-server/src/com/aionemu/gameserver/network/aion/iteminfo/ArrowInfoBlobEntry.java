package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * @author Rolandas
 */
public class ArrowInfoBlobEntry extends ItemBlobEntry {

	public ArrowInfoBlobEntry() {
		super(ItemBlobType.SLOTS_ARROW);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Item item = ownerItem;
		writeQ(buf, ItemSlot.getSlotFor(item.getItemTemplate().getItemSlot()).getSlotIdMask());
		writeQ(buf, 0);
	}

	@Override
	public int getSize() {
		return 8;
	}

}
