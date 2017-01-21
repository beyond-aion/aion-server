package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.stats.calc.functions.StatRateFunction;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * @author Rolandas
 */
public class BonusInfoBlobEntry extends ItemBlobEntry {

	public BonusInfoBlobEntry() {
		super(ItemBlobType.STAT_BONUSES);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		writeH(buf, modifier.getName().getItemStoneMask());
		writeD(buf, modifier.getValue() * modifier.getName().getSign());
		writeC(buf, modifier instanceof StatRateFunction ? 1 : 0);
	}

	@Override
	public int getSize() {
		return 7;
	}

}
