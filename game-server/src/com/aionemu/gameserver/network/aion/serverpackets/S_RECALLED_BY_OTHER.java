package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author SVDNESS
 */

//New packet. Player recall logic.
public class S_RECALLED_BY_OTHER extends AionServerPacket {
	public static final int RECALL_REQUEST_ID = 0x0F44;
	private final String casterName;
	private final int skillId;
	private final int timeSeconds;

	public S_RECALLED_BY_OTHER(String casterName, int skillId, int timeSeconds) {
		this.casterName = casterName;
		this.skillId = skillId;
		this.timeSeconds = timeSeconds;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(0); //Retail always sends 0.
		writeS(casterName);
		writeH(skillId);
		writeH(timeSeconds);
	}
}