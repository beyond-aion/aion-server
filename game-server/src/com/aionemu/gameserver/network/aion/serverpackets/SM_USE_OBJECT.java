package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_USE_OBJECT extends AionServerPacket {

	private int playerObjId;
	private int targetObjId;
	private int time;
	private int actionType;

	public SM_USE_OBJECT(int playerObjId, int targetObjId, int time, int actionType) {
		super();
		this.playerObjId = playerObjId;
		this.targetObjId = targetObjId;
		this.time = time;
		this.actionType = actionType;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjId);
		writeD(targetObjId);
		writeD(time);
		writeC(actionType);
	}
}
