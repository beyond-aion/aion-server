package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is used to teleport player and port animation
 * 
 * @author Luno, orz, xTz
 */
public class SM_TELEPORT_LOC extends AionServerPacket {

	private byte portAnimation;
	private int mapId;
	private int instanceId;
	private float x, y, z;
	private byte heading;
	private boolean isInstance;

	public SM_TELEPORT_LOC(int mapId, int instanceId, float x, float y, float z, byte heading, TeleportAnimation portAnimation) {
		this.isInstance = DataManager.WORLD_MAPS_DATA.getTemplate(mapId).isInstance();
		this.instanceId = instanceId;
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.portAnimation = portAnimation.getId();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(portAnimation);
		writeD(mapId);// new 4.3 NA -->old //writeH(mapId & 0xFFFF);
		writeD(isInstance ? instanceId : mapId); // mapId | instanceId
		writeF(x);
		writeF(y);
		writeF(z);
		writeC(heading);
	}
}
