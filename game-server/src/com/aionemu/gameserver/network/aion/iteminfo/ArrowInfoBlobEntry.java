package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * @author Rolandas
 */
public class ArrowInfoBlobEntry extends ItemBlobEntry {

	public ArrowInfoBlobEntry() {
		super(ItemBlobType.SLOTS_ARROW);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeQ(buf, ItemSlot.getSlotFor(ownerItem.getItemTemplate().getItemSlot()).getSlotIdMask());
		writeQ(buf, 0);
	}

	@Override
	public int getSize() {
		return 8;
	}

}
