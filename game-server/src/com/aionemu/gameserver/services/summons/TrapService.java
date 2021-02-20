package com.aionemu.gameserver.services.summons;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.aionemu.gameserver.model.gameobjects.Trap;

/**
 * @author Sykra
 */
public class TrapService {

	private static final int TRAP_LIMIT_PER_OWNER = 2;
	private static final Map<Integer, Queue<Trap>> registeredTraps = new ConcurrentHashMap<>();

	public static void registerTrap(int ownerObjId, Trap trap, boolean removeExcessTraps) {
		if (trap == null || trap.isDead())
			return;
		Queue<Trap> traps = registeredTraps.computeIfAbsent(ownerObjId, (objId) -> new ConcurrentLinkedQueue<>());
		traps.offer(trap);
		if (removeExcessTraps) {
			while (!traps.isEmpty() && traps.size() > TRAP_LIMIT_PER_OWNER) {
				Trap firstPlacedTrap = traps.poll();
				if (firstPlacedTrap != null)
					firstPlacedTrap.getController().delete();
			}
		}
	}

	public static void unregisterTrap(int trapObjId) {
		Collection<Queue<Trap>> allTraps = registeredTraps.values();
		allTraps.forEach(traps -> traps.removeIf(trap -> trap.getObjectId() == trapObjId));
		allTraps.removeIf(Collection::isEmpty);
	}

}
