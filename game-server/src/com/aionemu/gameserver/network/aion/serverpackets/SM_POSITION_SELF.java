package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.clientpackets.CM_POSITION_SELF;

/**
 * Instantly moves the player to the given position and cancels movement on client side (just like {@link SM_POSITION}). The client responds with
 * {@link CM_POSITION_SELF} afterward.
 *
 * @author cura
 */
public class SM_POSITION_SELF extends AionServerPacket {

	private final float x, y, z;
	private final byte heading;
	private final byte reason;

	public SM_POSITION_SELF(float x, float y, float z, byte heading) {
		this(x, y, z, heading, (byte) 0);
	}

	/**
	 * @param reason
	 *          Why the client is moved, read from the packet after the heading. Retail sends 8 when a flying player reported a position outside
	 *          the flight areas; other reasons (4, 5, 6) reset the client's area manager, so an unknown one is safer than a wrong one.
	 */
	public SM_POSITION_SELF(float x, float y, float z, byte heading, byte reason) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.reason = reason;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeF(x);
		writeF(y);
		writeF(z);
		writeC(heading);
		writeC(reason);
	}
}
