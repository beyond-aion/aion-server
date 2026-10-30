package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * @author Rolandas
 */
public class PremiumOptionInfoBlobEntry extends ItemBlobEntry {

	public PremiumOptionInfoBlobEntry() {
		super(ItemBlobType.PREMIUM_OPTION);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeC(buf, !ownerItem.isIdentified() ? -1 : ownerItem.getBonusStatsId());
		writeC(buf, !ownerItem.isIdentified() ? 0 : ownerItem.getTuneCount());
		writeC(buf, 0);
	}

	@Override
	public int getSize() {
		return 3;
	}

}
