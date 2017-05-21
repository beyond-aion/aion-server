package com.aionemu.gameserver.model.instance.instanceposition;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author xTz
 */
public abstract class GeneralInstancePosition implements InstancePositionHandler {

	protected int mapId;
	protected int instanceId;

	@Override
	public final void initialize(int mapId, int instanceId) {
		this.mapId = mapId;
		this.instanceId = instanceId;
	}

	protected void teleport(Player player, float x, float y, float z, byte h) {
		TeleportService.teleportTo(player, mapId, instanceId, x, y, z, h);
	}
}
