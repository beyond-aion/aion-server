package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_SUMMON_PANEL_REMOVE extends AionServerPacket {

	private int skillId;

	public SM_SUMMON_PANEL_REMOVE(int skillId) {
		this.skillId = skillId;
	}

	@Override
	protected void writeImpl(AionConnection con) {

		writeH(skillId); // skillId
		if (skillId != 0)
			writeC(1); // unk = 1
		else
			writeC(0); // unk
	}
}
