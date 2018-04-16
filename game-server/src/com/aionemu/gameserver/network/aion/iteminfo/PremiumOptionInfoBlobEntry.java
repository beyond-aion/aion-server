package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * @author Rolandas
 */
public class PremiumOptionInfoBlobEntry extends ItemBlobEntry {

	public PremiumOptionInfoBlobEntry() {
		super(ItemBlobType.PREMIUM_OPTION);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		writeC(buf, !ownerItem.isIdentified() ? -1 : ownerItem.getBonusStatsId());
		writeC(buf, !ownerItem.isIdentified() ? 0 : ownerItem.getTuneCount());
		writeC(buf, 0);
	}

	@Override
	public int getSize() {
		return 3;
	}

}
