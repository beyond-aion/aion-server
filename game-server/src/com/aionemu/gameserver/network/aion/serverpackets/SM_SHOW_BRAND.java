package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_SHOW_BRAND extends AionServerPacket {

	private final int brandId;
	private final int targetObjectId;
	private final boolean isLeague;

	public SM_SHOW_BRAND(int brandId, int targetObjectId) {
		this(brandId, targetObjectId, false);
	}

	public SM_SHOW_BRAND(int brandId, int targetObjectId, boolean isLeague) {
		this.brandId = brandId;
		this.targetObjectId = targetObjectId;
		this.isLeague = isLeague;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(isLeague ? 0x02 : 0x01);
		writeD(0x01); // unk
		writeD(brandId);
		writeD(targetObjectId);
		if (isLeague) {
			writeD(2);
			writeD(0);
			writeD(0);
		}
	}

}
