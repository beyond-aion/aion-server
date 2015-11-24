package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author orz, Yeats
 */
public class SM_GATHER_ANIMATION extends AionServerPacket {

	private int playerObjId;
	private int gatherableObjId;
	private int skillId;
	private int action;

	public SM_GATHER_ANIMATION(int playerObjId, int gatherableObjId, int skillId, int action) {
		this.playerObjId = playerObjId;
		this.gatherableObjId = gatherableObjId;
		this.skillId = skillId;
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeD(gatherableObjId);
		writeH(skillId);
		writeC(action);
	}
}
