package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;

/**
 * @author ATracer
 */
public class MovementNotifyTask extends AbstractFIFOPeriodicTaskManager<Creature> {

	private static final class SingletonHolder {

		private static final MovementNotifyTask INSTANCE = new MovementNotifyTask();
	}

	public static MovementNotifyTask getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public MovementNotifyTask() {
		super(500);
	}

	@Override
	protected void callTask(Creature creature) {
		if (creature.isDead())
			return;

		// In Reshanta:
		// max_move_broadcast_count is 200 and
		// min_move_broadcast_range is 75, as in client WorldId.xml
		int limit = creature.getWorldId() == 400010000 ? 200 : Integer.MAX_VALUE;
		creature.getKnownList().stream()
			.filter(o -> o.get() instanceof Npc)
			.limit(limit)
			.forEach(o -> notifyCreatureMoved((Npc) o.get(), creature));
	}

	void notifyCreatureMoved(Npc npc, Creature creature) {
		try {
			if (npc.getAi().getState() == AIState.DIED || npc.isDead()) {
				if (npc.getAi().isLogging()) {
					AILogger.moveinfo(npc, "WARN: NPC died but still in knownlist");
				}
				return;
			}
			npc.getAi().onCreatureEvent(AIEventType.CREATURE_MOVED, creature);
		} catch (Exception ex) {
			log.error("Could not notify {} about movement of {}", npc, creature, ex);
		}
	}

	@Override
	protected String getCalledMethodName() {
		return "notifyOnMove()";
	}
}
