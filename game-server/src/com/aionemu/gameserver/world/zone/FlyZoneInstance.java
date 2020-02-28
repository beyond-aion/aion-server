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
		if (super.onEnter(creature)) {
			creature.setInsideZoneType(ZoneType.FLY);
			if (creature instanceof Player) {
				((Player) creature).getController().onEnterFlyArea();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean onLeave(Creature creature) {
		if (super.onLeave(creature)) {
			creature.unsetInsideZoneType(ZoneType.FLY);
			if (!creature.isInsideZoneType(ZoneType.FLY) && creature instanceof Player)
				((Player) creature).getController().onLeaveFlyArea();
			return true;
		} else
			return false;
	}
}
