package com.aionemu.gameserver.world.zone.handler;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author MrPoke
 */
public interface ZoneHandler {

	void onEnterZone(Creature player, ZoneInstance zone);

	void onLeaveZone(Creature player, ZoneInstance zone);
}
