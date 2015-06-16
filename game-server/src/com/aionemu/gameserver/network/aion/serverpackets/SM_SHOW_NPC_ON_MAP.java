package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Lyahim
 */
public class SM_SHOW_NPC_ON_MAP extends AionServerPacket {

	private int npcid, worldid;
	private float x, y, z;

	public SM_SHOW_NPC_ON_MAP(int npcid, int worldid, float x, float y, float z) {
		this.npcid = npcid;
		this.worldid = worldid;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(this.npcid);
		writeD(this.worldid);
		writeD(this.worldid);
		writeF(this.x);
		writeF(this.y);
		writeF(this.z);
	}
}
