package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author cura
 */
public class SM_PLAYER_MOVE extends AionServerPacket {

	private float x;
	private float y;
	private float z;
	private byte heading;

	public SM_PLAYER_MOVE(float x, float y, float z, byte heading) {
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
