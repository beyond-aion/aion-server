package com.aionemu.gameserver.model.instance.instanceposition;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author xTz
 */
public class GenerealInstancePosition implements InstancePositionHandler {

	protected int mapId;
	protected int instanceId;

	@Override
	public void initsialize(Integer mapId, int instanceId) {
		this.mapId = mapId;
		this.instanceId = instanceId;
	}

	@Override
	public void port(Player player, int zone, int position) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	protected void teleport(Player player, float x, float y, float z, byte h) {
		TeleportService2.teleportTo(player, mapId, instanceId, x, y, z, h);
	}
}
