package com.aionemu.gameserver.network.aion.iteminfo;

import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

import io.netty.buffer.ByteBuf;

/**
 * @author Rolandas
 */
public class StigmaShardInfoBlobEntry extends ItemBlobEntry {

	public StigmaShardInfoBlobEntry() {
		super(ItemBlobType.STIGMA_SHARD);
	}

	@Override
	public void writeThisBlob(ByteBuf buf) {
		writeD(buf, 0);
	}

	@Override
	public int getSize() {
		return 4;
	}

}
