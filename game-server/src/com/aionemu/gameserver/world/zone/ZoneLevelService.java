package com.aionemu.gameserver.world.zone;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class ZoneLevelService {

	private static final long DROWN_PERIOD = 2000;

	/**
	 * Check water level (start drowning) and map death level (die)
	 */
	public static void checkZoneLevels(Player player) {
		World world = World.getInstance();
		float z = player.getZ();

		if (player.isDead())
			return;

		if (z < world.getWorldMap(player.getWorldId()).getDeathLevel()) {
			player.getController().die();
			return;
		}

		float noseHeight = player.getPlayerAppearance().getBoundHeight() - 0.1f;
		if (z + noseHeight < world.getWorldMap(player.getWorldId()).getWaterLevel())
			startDrowning(player);
		else
			stopDrowning(player);
	}

	private static void stopDrowning(Player player) {
		if (player.getController().hasTask(TaskId.DROWN))
			player.getController().cancelTask(TaskId.DROWN);
	}

	private static void startDrowning(Player player) {
		if (player.getController().hasTask(TaskId.DROWN))
			return;
		player.getController().addTask(TaskId.DROWN, ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				int value = Math.round(player.getLifeStats().getMaxHp() / 10f);
				if (player.getLifeStats().reduceHp(TYPE.DROWNING, value, 0, LOG.REGULAR, player) == 0)
					stopDrowning(player);
			}
		}, 0, DROWN_PERIOD));
	}
}
