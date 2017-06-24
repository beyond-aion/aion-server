package com.aionemu.gameserver.taskmanager.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;

/**
 * @author ATracer
 */
public class MovementNotifyTask extends AbstractFIFOPeriodicTaskManager<Creature> {

	private static Map<Integer, int[]> moveBroadcastCounts = new HashMap<>();

	static {
		Iterator<WorldMapTemplate> iter = DataManager.WORLD_MAPS_DATA.iterator();
		while (iter.hasNext())
			moveBroadcastCounts.put(iter.next().getMapId(), new int[2]);
	}

	private static final class SingletonHolder {

		private static final MovementNotifyTask INSTANCE = new MovementNotifyTask();
	}

	public static MovementNotifyTask getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private final MoveNotifier MOVE_NOTIFIER = new MoveNotifier();

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
		int iterations = creature.getKnownList().forEachNpcWithOwner(MOVE_NOTIFIER, limit);

		if (!(creature instanceof Player)) {
			int[] maxCounts = moveBroadcastCounts.get(creature.getWorldId());
			synchronized (maxCounts) {
				if (iterations > maxCounts[0]) {
					maxCounts[0] = iterations;
					maxCounts[1] = creature.getObjectTemplate().getTemplateId();
				}
			}
		}
	}

	public String[] dumpBroadcastStats() {
		List<String> lines = new ArrayList<>();
		lines.add("------- Movement broadcast counts -------");
		for (Entry<Integer, int[]> entry : moveBroadcastCounts.entrySet()) {
			lines.add("WorldId=" + entry.getKey() + ": " + entry.getValue()[0] + " (NpcId " + entry.getValue()[1] + ")");
		}
		lines.add("-----------------------------------------");
		return lines.toArray(new String[0]);
	}

	@Override
	protected String getCalledMethodName() {
		return "notifyOnMove()";
	}

	private class MoveNotifier implements BiConsumer<Npc, VisibleObject> {

		@Override
		public void accept(Npc object, VisibleObject owner) {

			if (object.getAi().getState() == AIState.DIED || object.isDead()) {
				if (object.getAi().isLogging()) {
					AILogger.moveinfo(object, "WARN: NPC died but still in knownlist");
				}
				return;
			}
			object.getAi().onCreatureEvent(AIEventType.CREATURE_MOVED, (Creature) owner);
		}

	}
}
