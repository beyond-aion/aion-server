package com.aionemu.gameserver.model.instance.instanceposition;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public interface InstancePositionHandler {

	void initsialize(Integer mapId, int instanceId);

	void port(Player player, int zone, int position);
}
