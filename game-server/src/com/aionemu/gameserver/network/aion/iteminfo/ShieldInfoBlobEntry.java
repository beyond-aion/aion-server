package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * This blob is sent for shields. It keeps info about slots that shield can be equipped to.
 *
 * @author -Nemesiss-, Rolandas
 */
public class ShieldInfoBlobEntry extends ItemBlobEntry {

	ShieldInfoBlobEntry() {
		super(ItemBlobType.SLOTS_SHIELD);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeQ(buf, ItemSlot.getSlotFor(ownerItem.getItemTemplate().getItemSlot()).getSlotIdMask());
		writeQ(buf, 0); // TODO! secondary slot?
		writeDyeInfo(buf, ownerItem.getItemColor()); // 4 bytes
	}

	@Override
	public int getSize() {
		return 20;
	}
}
