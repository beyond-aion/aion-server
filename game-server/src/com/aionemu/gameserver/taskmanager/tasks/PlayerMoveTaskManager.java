package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ATracer
 */
public class PlayerMoveTaskManager extends AbstractPeriodicTaskManager {

	private final ConcurrentHashMap<Integer, Creature> movingPlayers = new ConcurrentHashMap<Integer, Creature>();

	private PlayerMoveTaskManager() {
		super(200);
	}

	public void addPlayer(Creature player) {
		movingPlayers.put(player.getObjectId(), player);
	}

	public void removePlayer(Creature player) {
		movingPlayers.remove(player.getObjectId());
	}

	@Override
	public void run() {
		for (ConcurrentHashMap.Entry<Integer, Creature> e : movingPlayers.entrySet()) {
			Creature player = e.getValue();
			player.getMoveController().moveToDestination();
		}
	}

	public static final PlayerMoveTaskManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static final class SingletonHolder {

		private static final PlayerMoveTaskManager INSTANCE = new PlayerMoveTaskManager();
	}
}
