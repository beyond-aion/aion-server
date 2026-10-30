package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.model.stats.calc.functions.StatRateFunction;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * @author Rolandas
 */
public class BonusInfoBlobEntry extends ItemBlobEntry {

	public BonusInfoBlobEntry() {
		super(ItemBlobType.STAT_BONUSES);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeH(buf, modifier.getName().getItemStoneMask());
		writeD(buf, modifier.getValue() * modifier.getName().getSign());
		writeC(buf, modifier instanceof StatRateFunction ? 1 : 0);
	}

	@Override
	public int getSize() {
		return 7;
	}

}
