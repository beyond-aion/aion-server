package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * This blob sends info about conditioning.
 * 
 * @author -Nemesiss-, Rolandas
 */
public class ConditioningInfoBlobEntry extends ItemBlobEntry {

	ConditioningInfoBlobEntry() {
		super(ItemBlobType.CONDITIONING_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeD(buf, ownerItem.getChargePoints());
	}

	@Override
	public int getSize() {
		return 4;
	}
}
