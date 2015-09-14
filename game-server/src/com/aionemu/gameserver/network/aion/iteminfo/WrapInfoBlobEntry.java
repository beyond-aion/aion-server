package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * @author Rolandas Written the last, after all BonusInfoBlobEntry
 */
public class WrapInfoBlobEntry extends ItemBlobEntry {

	WrapInfoBlobEntry() {
		super(ItemBlobType.WRAP_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		writeC(buf, ownerItem.getPackCount());
	}

	@Override
	public int getSize() {
		return 1;
	}

}
