package com.aionemu.gameserver.world.zone;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;

/**
 * @author ATracer
 */
public class ZoneUpdateService extends AbstractFIFOPeriodicTaskManager<Creature> {

	private ZoneUpdateService() {
		super(500);
	}

	@Override
	protected void callTask(Creature creature) {
		// validate all zones irrespective of the current zone
		creature.revalidateZones();
		if (creature instanceof Player) {
			ZoneLevelService.checkZoneLevels((Player) creature);
		}
	}

	@Override
	protected String getCalledMethodName() {
		return "ZoneUpdateService()";
	}

	public static ZoneUpdateService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final ZoneUpdateService instance = new ZoneUpdateService();
	}

}
