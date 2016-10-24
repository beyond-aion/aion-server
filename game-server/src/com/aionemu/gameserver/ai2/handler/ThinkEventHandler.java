package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AI2Logger;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public class ThinkEventHandler {

	/**
	 * @param npcAI
	 */
	public static void onThink(NpcAI2 npcAI) {
		if (npcAI.isAlreadyDead()) {
			AI2Logger.info(npcAI, "can't think in dead state");
			return;
		}
		if (!npcAI.tryLockThink()) {
			AI2Logger.info(npcAI, "can't acquire think lock");
			return;
		}
		try {
			if (!npcAI.getOwner().getPosition().isMapRegionActive() || npcAI.getSubState() == AISubState.FREEZE) {
				thinkInInactiveRegion(npcAI);
				return;
			}
			if (npcAI.isLogging()) {
				AI2Logger.info(npcAI, "think in ai state: " + npcAI.getState());
			}
			switch (npcAI.getState()) {
				case FIGHT:
					thinkAttack(npcAI);
					break;
				case IDLE:
					thinkIdle(npcAI);
					break;
			}
		} finally {
			npcAI.unlockThink();
		}
	}

	/**
	 * @param npcAI
	 */
	private static void thinkInInactiveRegion(NpcAI2 npcAI) {
		if (npcAI.isInState(AIState.WALKING)) {
			WalkManager.stopWalking(npcAI);
			return;
		}
		if (!npcAI.canThink()) {
			return;
		}
		if (npcAI.isLogging()) {
			AI2Logger.info(npcAI, "think (inactive region) in ai state: " + npcAI.getState());
		}
		switch (npcAI.getState()) {
			case FIGHT:
				thinkAttack(npcAI);
				break;
			default:
				if (!npcAI.getOwner().isAtSpawnLocation()) {
					npcAI.onGeneralEvent(AIEventType.NOT_AT_HOME);
				}
		}

	}

	/**
	 * @param npcAI
	 */
	public static void thinkAttack(NpcAI2 npcAI) {
		Npc npc = npcAI.getOwner();
		Creature mostHated = npc.getAggroList().getMostHated();
		if (mostHated != null && !mostHated.getLifeStats().isAlreadyDead()) {
			npcAI.onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
		} else {
			npc.getQueuedSkills().clear();
			npc.getGameStats().setLastSkill(null);
			npc.getGameStats().resetFightStats();
			npc.getMoveController().recallPreviousStep();
			npcAI.onGeneralEvent(AIEventType.ATTACK_FINISH);
			npcAI.onGeneralEvent(npc.isAtSpawnLocation() ? AIEventType.BACK_HOME : AIEventType.NOT_AT_HOME);
		}
	}

	/**
	 * @param npcAI
	 */
	public static void thinkIdle(NpcAI2 npcAI) {
		if (npcAI.isMoveSupported() && npcAI.getOwner().isWalker())
			WalkManager.startWalking(npcAI);
	}
}
