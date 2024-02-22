package com.aionemu.gameserver.taskmanager.tasks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;

/**
 * @author ATracer, Rolandas
 */
public class MoveTaskManager extends AbstractPeriodicTaskManager {

	private static final int UPDATE_PERIOD = 200;
	private final Map<Integer, Creature> movingCreatures = new ConcurrentHashMap<>();

	private MoveTaskManager() {
		super(UPDATE_PERIOD);
	}

	public void addCreature(Creature creature) {
		if (!creature.isSpawned()) { // log with stack trace to find the cause
			LoggerFactory.getLogger(MoveTaskManager.class)
				.warn("Failed attempt to add " + creature + " to moving creatures (despawned objects cannot move)", new UnsupportedOperationException());
			return;
		}
		movingCreatures.putIfAbsent(creature.getObjectId(), creature);
	}

	public boolean removeCreature(Creature creature) {
		return movingCreatures.remove(creature.getObjectId()) != null;
	}

	@Override
	public void run() {
		movingCreatures.values().parallelStream().forEach(creature -> {
			if (!creature.isSpawned()) { // can despawn concurrently, while this thread is already running
				if (removeCreature(creature)) // should have been removed via onDespawn (MoveController#abortMove())
					LoggerFactory.getLogger(MoveTaskManager.class).warn(creature + " was still in moving creatures list but already despawned");
				return;
			}
			creature.getMoveController().moveToDestination();
			if (creature.getAi().isDestinationReached()) {
				removeCreature(creature);
				creature.getAi().onGeneralEvent(AIEventType.MOVE_ARRIVED);
				ZoneUpdateService.getInstance().add(creature);
			} else {
				creature.getAi().onGeneralEvent(AIEventType.MOVE_VALIDATE);
			}
		});
	}

	public static MoveTaskManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static final class SingletonHolder {

		private static final MoveTaskManager INSTANCE = new MoveTaskManager();
	}

}
