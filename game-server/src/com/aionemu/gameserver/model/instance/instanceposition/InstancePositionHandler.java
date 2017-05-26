package com.aionemu.gameserver.model.instance.instanceposition;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public interface InstancePositionHandler {

	void initialize(int mapId, int instanceId);

	void port(Player player, int zone, int position);
}
