package com.aionemu.gameserver.world.zone;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneType;

/**
 * @author SVDNESS
 */
// Neutral zone: client-side disablePvP area, represented in static data as zone_type="PVP" with no flags.
public class DisablePvPZoneInstance extends ZoneInstance {

	public DisablePvPZoneInstance(int mapId, ZoneInfo template) {
		super(mapId, template);
	}

	@Override
	public synchronized boolean onEnter(Creature creature) {
		if (super.onEnter(creature)) {
			creature.setInsideZoneType(ZoneType.DISABLE_PVP);
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean onLeave(Creature creature) {
		if (super.onLeave(creature)) {
			creature.unsetInsideZoneType(ZoneType.DISABLE_PVP);
			return true;
		}
		return false;
	}
}