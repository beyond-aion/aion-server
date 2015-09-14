package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_LEAVE_MEMBER extends AionServerPacket {

	private String name;
	private String name1;
	private int playerObjId;
	private int msgId;

	public SM_LEGION_LEAVE_MEMBER(int msgId, int playerObjId, String name) {
		this.msgId = msgId;
		this.playerObjId = playerObjId;
		this.name = name;
	}

	public SM_LEGION_LEAVE_MEMBER(int msgId, int playerObjId, String name, String name1) {
		this.msgId = msgId;
		this.playerObjId = playerObjId;
		this.name = name;
		this.name1 = name1;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeC(0x00); // isMember ? 1 : 0
		writeD(0x00); // unix time for log off
		writeD(msgId);
		writeS(name);
		writeS(name1);
	}
}
