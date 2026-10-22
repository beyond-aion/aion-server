package com.aionemu.gameserver.world.zone;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;

/**
 * @author MrPoke
 */
public class SiegeZoneInstance extends ZoneInstance {

	public SiegeZoneInstance(int mapId, ZoneInfo template) {
		super(mapId, template);
	}

	@Override
	public boolean isIgnored(Creature creature) {
		return super.isIgnored(creature) && !(creature instanceof SiegeNpc);
	}
}
