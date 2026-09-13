package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Answers the selection the client made in the decomposable window. It carries no items, only whether the chosen one was handed out.
 *
 * @author xTz
 */
public class SM_SECONDARY_SHOW_DECOMPOSABLE extends AionServerPacket {

	public static final int GRANTED = 0;
	public static final int NOT_GRANTED = 1;

	private final int objectId;
	private final int result;

	public SM_SECONDARY_SHOW_DECOMPOSABLE(int objectId, int result) {
		this.objectId = objectId;
		this.result = result;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeD(0); // the object id is a 64 bit field, ours never fill the upper half
		writeC(result);
	}

}
