package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.WorldMapType;

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
	private int clientWorldId;

	public SM_TELEPORT_LOC(int mapId, int instanceId, float x, float y, float z, byte heading, TeleportAnimation portAnimation) {
		this.isInstance = DataManager.WORLD_MAPS_DATA.getTemplate(mapId).isInstance();
		this.instanceId = instanceId;
		this.mapId = mapId;
		// an open world twin is a world id of its own for the client, so a channel change has to carry it or the client stays where it is
		WorldMapType world = instanceId > 1 ? WorldMapType.getWorld(mapId) : null;
		this.clientWorldId = !isInstance && instanceId > 1 && (world == null || !world.isPersonal()) ? mapId + instanceId - 1 : mapId;
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
		writeD(isInstance ? instanceId : clientWorldId); // mapId | instanceId
		writeF(x);
		writeF(y);
		writeF(z);
		writeC(heading);
	}
}
