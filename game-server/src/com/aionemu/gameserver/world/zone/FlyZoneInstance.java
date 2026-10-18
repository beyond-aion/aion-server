package com.aionemu.gameserver.world.zone;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneType;

/**
 * @author MrPoke
 */
public class FlyZoneInstance extends ZoneInstance {

	public FlyZoneInstance(int mapId, ZoneInfo template) {
		super(mapId, template);
	}

	@Override
	public synchronized boolean onEnter(Creature creature) {
		if (!super.onEnter(creature))
			return false;
		boolean wasInFlyZone = creature.isInsideFlyZone();
		creature.setInsideZoneType(ZoneType.FLY);
		if (!wasInFlyZone && creature instanceof Player player && player.isInsideFlyZone())
			player.getController().onEnterFlyArea();
		return true;
	}

	@Override
	public synchronized boolean onLeave(Creature creature) {
		if (!super.onLeave(creature))
			return false;
		boolean wasInFlyZone = creature.isInsideFlyZone();
		creature.unsetInsideZoneType(ZoneType.FLY);
		if (wasInFlyZone && creature instanceof Player player && !player.isInsideFlyZone())
			player.getController().onLeaveFlyArea();
		return true;
	}
}
