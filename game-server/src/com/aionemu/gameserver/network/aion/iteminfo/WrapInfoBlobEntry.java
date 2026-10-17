package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * @author Rolandas Written the last, after all BonusInfoBlobEntry
 */
public class WrapInfoBlobEntry extends ItemBlobEntry {

	WrapInfoBlobEntry() {
		super(ItemBlobType.WRAP_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeC(buf, ownerItem.getPackCount());
	}

	@Override
	public int getSize() {
		return 1;
	}

}
