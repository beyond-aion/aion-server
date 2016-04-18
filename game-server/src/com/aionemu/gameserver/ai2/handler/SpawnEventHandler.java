package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;

/**
 * @author ATracer
 */
public class SpawnEventHandler {

	/**
	 * @param npcAI
	 */
	public static void onSpawn(NpcAI2 npcAI) {
		if (npcAI.setStateIfNot(AIState.IDLE)) {
			if (npcAI.getOwner().getPosition().isMapRegionActive()) {
				npcAI.think();
			}
		}
	}

	/**
	 * @param npcAI
	 */
	public static void onDespawn(NpcAI2 npcAI) {
		npcAI.setStateIfNot(AIState.DESPAWNED);
	}

	/**
	 * @param npcAI
	 */
	public static void onRespawn(NpcAI2 npcAI) {
		npcAI.getOwner().getMoveController().resetMove(false);
	}

}
