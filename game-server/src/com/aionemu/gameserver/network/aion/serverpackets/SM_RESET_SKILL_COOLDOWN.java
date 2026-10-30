package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_RESET_SKILL_COOLDOWN extends AionServerPacket {

	private int skillId;

	public SM_RESET_SKILL_COOLDOWN(int skillId) {
		this.skillId = skillId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		writeC(skillId == 0 ? 0 : 1); // the client reads only the skill ID
	}
}
