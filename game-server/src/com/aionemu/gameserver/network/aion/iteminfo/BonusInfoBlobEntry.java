package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.configs.administration.DeveloperConfig;
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
		if (DeveloperConfig.ITEM_STAT_ID > 0) {
			writeH(buf, DeveloperConfig.ITEM_STAT_ID);
			writeD(buf, 10);
			writeC(buf, 0);
		}
		else {
			writeH(buf, modifier.getName().getItemStoneMask());
			writeD(buf, modifier.getValue() * modifier.getName().getSign());
			writeC(buf, modifier instanceof StatRateFunction ? 1 : 0);
		}
	}

	@Override
	public int getSize() {
		return 7;
	}

}
