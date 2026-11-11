package com.aionemu.gameserver.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.taskmanager.tasks.*;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;

/**
 * @author lord_rex and MrPoke based on l2j-free engines. This can be used for periodic calls.
 */
public abstract class AbstractPeriodicTaskManager {

	protected static final Logger log = LoggerFactory.getLogger(AbstractPeriodicTaskManager.class);

	public AbstractPeriodicTaskManager(int period) {
		log.info("{} initialized", getClass().getSimpleName());
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::run, Rnd.get(0, period), period);
	}

	protected abstract void run();

	public static void startAll() {
		ExpireTimerTask.getInstance();
		TemporaryTradeTimeTask.getInstance();
		TeamStatUpdater.getInstance();
		TeamMoveUpdater.getInstance();
		MovementNotifyTask.getInstance();
		MoveTaskManager.getInstance();
		PlayerMoveTaskManager.getInstance();
		ZoneUpdateService.getInstance();
	}
}
