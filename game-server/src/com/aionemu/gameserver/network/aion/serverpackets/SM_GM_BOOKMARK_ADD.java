package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Yeats.
 */
public class SM_GM_BOOKMARK_ADD extends AionServerPacket {

	// //fsc 64 sdfffd [Teleportatiions Platz2] 120010000 230 250 290 1

	private String name;
	private int worldId;
	private float x, y, z;

	public SM_GM_BOOKMARK_ADD(String name, int worldId, float x, float y, float z) {
		this.name = name;
		this.worldId = worldId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(name);
		writeD(worldId);
		writeF(x);
		writeF(y);
		writeF(z);
	}
}
