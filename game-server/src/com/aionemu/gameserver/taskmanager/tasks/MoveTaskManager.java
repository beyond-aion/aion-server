package com.aionemu.gameserver.taskmanager.tasks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;

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

	public static final int UPDATE_PERIOD = 100;
	private final Map<Integer, Creature> movingCreatures = new ConcurrentHashMap<>();
	private final Consumer<Creature> CREATURE_MOVE_PREDICATE = new Consumer<Creature>() {

		@Override
		public void accept(Creature creature) {
			if (creature == null) // concurrent iterating over movingCreatures.values() can cause calling this with an already removed entry (which then is null)
				return;
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
		movingCreatures.putIfAbsent(creature.getObjectId(), creature);
	}

	public void removeCreature(Creature creature) {
		movingCreatures.remove(creature.getObjectId());
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
