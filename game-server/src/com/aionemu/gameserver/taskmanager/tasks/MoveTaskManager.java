package com.aionemu.gameserver.taskmanager.tasks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.taskmanager.parallel.ForEach;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;

/**
 * @author ATracer
 * @reworked Rolandas, parallelized by using Fork/Join framework
 */
public class MoveTaskManager extends AbstractPeriodicTaskManager {

	private static final int UPDATE_PERIOD = 200;
	private final Map<Integer, Creature> movingCreatures = new ConcurrentHashMap<>();
	private final Consumer<Creature> CREATURE_MOVE_PREDICATE = new Consumer<Creature>() {

		@Override
		public void accept(Creature creature) {
			if (creature == null) // concurrent iterating over movingCreatures can cause calling this with an already removed entry (which then is null)
				return;
			if (!creature.isSpawned()) { // can despawn concurrently, while this thread is already running
				if (removeCreature(creature)) // should have been removed via onDespawn (MoveController#abortMove())
					LoggerFactory.getLogger(MoveTaskManager.class).warn(creature + " was still in moving creatures list but already despawned");
				return;
			}
			creature.getMoveController().moveToDestination();
			if (creature.getAi().ask(AIQuestion.DESTINATION_REACHED)) {
				removeCreature(creature);
				creature.getAi().onGeneralEvent(AIEventType.MOVE_ARRIVED);
				ZoneUpdateService.getInstance().add(creature);
			} else {
				creature.getAi().onGeneralEvent(AIEventType.MOVE_VALIDATE);
			}

		}
	};

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
		ForkJoinTask<Creature> task = ForEach.newTask(movingCreatures.values(), CREATURE_MOVE_PREDICATE);
		if (task != null)
			ThreadPoolManager.getInstance().getForkingPool().invoke(task);
	}

	public static MoveTaskManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static final class SingletonHolder {

		private static final MoveTaskManager INSTANCE = new MoveTaskManager();
	}

}
