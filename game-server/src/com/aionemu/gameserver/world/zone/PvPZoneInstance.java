package com.aionemu.gameserver.world.zone;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneType;

/**
 * @author MrPoke
 */
public class PvPZoneInstance extends ZoneInstance {

	public PvPZoneInstance(int mapId, ZoneInfo template) {
		super(mapId, template);
	}

	@Override
	public synchronized boolean onEnter(Creature creature) {
		if (super.onEnter(creature)) {
			creature.setInsideZoneType(ZoneType.PVP);
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean onLeave(Creature creature) {
		if (super.onLeave(creature)) {
			creature.unsetInsideZoneType(ZoneType.PVP);
			return true;
		}
		return false;
	}
}
