package com.aionemu.gameserver.taskmanager.tasks;

import static com.aionemu.gameserver.taskmanager.parallel.ForEach.forEach;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;

import javolution.util.FastTable;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 * @reworked Rolandas, parallelized by using Fork/Join framework
 */
public class MoveTaskManager extends AbstractPeriodicTaskManager {

	private final ConcurrentHashMap<Integer, Creature> movingCreatures = new ConcurrentHashMap<>();

	public static final int UPDATE_PERIOD = 100;

	private final Predicate<Creature> CREATURE_MOVE_PREDICATE = new Predicate<Creature>() {

		@Override
		public boolean apply(Creature creature) {
			creature.getMoveController().moveToDestination();
			if (creature.getAi2().ask(AIQuestion.DESTINATION_REACHED)) {
				movingCreatures.remove(creature.getObjectId());
				creature.getAi2().onGeneralEvent(AIEventType.MOVE_ARRIVED);
				ZoneUpdateService.getInstance().add(creature);
			} else {
				creature.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
			}
			return true;
		}

	};

	private MoveTaskManager() {
		super(UPDATE_PERIOD);
	}

	public void addCreature(Creature creature) {
		movingCreatures.put(creature.getObjectId(), creature);
	}

	public void removeCreature(Creature creature) {
		movingCreatures.remove(creature.getObjectId());
	}

	@Override
	public void run() {
		final FastTable<Creature> copy = new FastTable<>();
		for (ConcurrentHashMap.Entry<Integer, Creature> e : movingCreatures.entrySet()) {
			copy.add(e.getValue());
		}
		ForkJoinTask<Creature> task = forEach(copy, CREATURE_MOVE_PREDICATE);
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
