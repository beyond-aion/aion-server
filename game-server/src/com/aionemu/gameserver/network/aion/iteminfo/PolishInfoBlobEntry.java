package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * @author Rolandas
 */
public class PolishInfoBlobEntry extends ItemBlobEntry {

	PolishInfoBlobEntry() {
		super(ItemBlobType.POLISH_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		// Idian charge value
		IdianStone stone = ownerItem.getIdianStone();
		writeD(buf, stone == null ? 0 : stone.getPolishCharge());
	}

	@Override
	public int getSize() {
		return 4;
	}

}
