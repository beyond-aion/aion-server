package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Instantly moves the object to the given position and cancels movement on client side if it is a player.
 * 
 * @author Sweetkr
 */
public class SM_POSITION extends AionServerPacket {

	private final VisibleObject object;

	public SM_POSITION(VisibleObject object) {
		this.object = object;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(object.getObjectId());
		writeF(object.getX());
		writeF(object.getY());
		writeF(object.getZ());
		writeC(object.getHeading());
	}
}
