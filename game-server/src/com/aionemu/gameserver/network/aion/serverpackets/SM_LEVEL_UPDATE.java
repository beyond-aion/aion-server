package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_LEVEL_UPDATE extends AionServerPacket {

	private int targetObjectId;
	private int effect;
	private int level;

	public SM_LEVEL_UPDATE(int targetObjectId, int effect, int level) {
		this.targetObjectId = targetObjectId;
		this.effect = effect;
		this.level = level;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeH(effect); // unk
		writeH(level);
		writeH(0x00); // unk
	}
}
