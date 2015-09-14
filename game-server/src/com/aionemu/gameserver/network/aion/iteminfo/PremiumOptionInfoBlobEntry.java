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
		if (ownerItem.getItemTemplate().canTune() && ownerItem.getRandomCount() == -1) {
			writeC(buf, -1);
			writeC(buf, 0);
		} else {
			writeC(buf, ownerItem.getBonusNumber());
			writeC(buf, ownerItem.getRandomCount());
		}
		writeC(buf, 0);
	}

	@Override
	public int getSize() {
		return 1 + 2;
	}

}
