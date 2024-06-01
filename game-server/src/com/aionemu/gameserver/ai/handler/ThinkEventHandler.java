package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public class ThinkEventHandler {

	public static void onThink(NpcAI npcAI) {
		if (npcAI.isDead()) {
			AILogger.info(npcAI, "can't think in dead state");
			return;
		}
		if (!npcAI.tryLockThink()) {
			AILogger.info(npcAI, "can't acquire think lock");
			return;
		}
		try {
			if (!npcAI.getOwner().getPosition().isMapRegionActive() || npcAI.getSubState() == AISubState.FREEZE) {
				thinkInInactiveRegion(npcAI);
				return;
			}
			if (npcAI.isLogging()) {
				AILogger.info(npcAI, "think in ai state: " + npcAI.getState());
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

	private static void thinkInInactiveRegion(NpcAI npcAI) {
		if (npcAI.isInState(AIState.WALKING)) {
			WalkManager.stopWalking(npcAI);
			return;
		}
		if (!npcAI.canThink()) {
			return;
		}
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "think (inactive region) in ai state: " + npcAI.getState());
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

	public static void thinkAttack(NpcAI npcAI) {
		Npc npc = npcAI.getOwner();
		Creature mostHated = npc.getAggroList().getMostHated();
		if (mostHated != null && !mostHated.isDead()) {
			npcAI.onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
		} else {
			npcAI.setSubStateIfNot(AISubState.NONE);
			npc.clearQueuedSkills();
			npc.getGameStats().setLastSkill(null);
			npc.getGameStats().resetFightStats();
			npc.getMoveController().recallPreviousStep();
			npcAI.onGeneralEvent(AIEventType.ATTACK_FINISH);
			npcAI.onGeneralEvent(npc.isAtSpawnLocation() ? AIEventType.BACK_HOME : AIEventType.NOT_AT_HOME);
		}
	}

	public static void thinkIdle(NpcAI npcAI) {
		if (npcAI.isMoveSupported() && npcAI.getOwner().isWalker())
			WalkManager.startWalking(npcAI);
	}
}
