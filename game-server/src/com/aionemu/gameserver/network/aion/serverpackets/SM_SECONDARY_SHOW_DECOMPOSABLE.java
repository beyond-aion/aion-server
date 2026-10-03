package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Answers the selection the client made in the decomposable window. It carries no items, only whether the chosen one was handed out.
 *
 * @author xTz
 */
public class SM_SECONDARY_SHOW_DECOMPOSABLE extends AionServerPacket {

	/*
	 * The client closes the window on both results. Retail never sends NOT_GRANTED, its picks always land in the cube, even past its size.
	 * We send it for a full cube, so a flood of picks can't overfill it, and for a box that is already gone.
	 * Result 2 keeps the window open with "can't acquire the item right now" and answers a per item drop quota, which we don't have.
	 */
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
