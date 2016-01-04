package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Lyahim
 */
public class SM_SHOW_NPC_ON_MAP extends AionServerPacket {

	private Player player;
	private int npcid, worldid;
	private float x, y, z;

	public SM_SHOW_NPC_ON_MAP(Player player, int npcid, int worldid, float x, float y, float z) {
		this.player = player;
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
		// default value: mapid + channelId(0)
		int instanceId = this.worldid;
		if (this.player.getPosition().getMapId() == this.worldid) {
			if (this.player.isInInstance())
				instanceId = this.player.getInstanceId();
			else
				instanceId = this.worldid + this.player.getInstanceId() - 1; // mapid + channelId (instanceId-1)
		}
		writeD(instanceId);

		writeF(this.x);
		writeF(this.y);
		writeF(this.z);
	}
}
