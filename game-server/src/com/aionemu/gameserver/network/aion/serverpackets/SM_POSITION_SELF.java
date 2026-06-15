package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.clientpackets.CM_POSITION_SELF;

/**
 * Instantly moves the player to the given position and cancels movement on client side (just like {@link SM_POSITION}).
 * The client responds with {@link CM_POSITION_SELF} afterward.
 * 
 * @author cura
 */
public class SM_POSITION_SELF extends AionServerPacket {

	private final float x, y, z;
	private final byte heading;

	public SM_POSITION_SELF(float x, float y, float z, byte heading) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeF(x);
		writeF(y);
		writeF(z);
		writeC(heading);
	}
}
