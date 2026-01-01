package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.AttackManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HEADING_UPDATE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class ThinkEventHandler {

	public static void onThink(NpcAI npcAI) {
		if (npcAI.isDead()) {
			AILogger.info(npcAI, "can't think in dead state");
			return;
		}
		if (!npcAI.setThinking()) {
			AILogger.info(npcAI, "skipped onThink because AI is already thinking");
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
			npcAI.unsetThinking();
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
		if (npc.getTarget() instanceof Creature target && npc.getAggroList().isHating(target)) {
			AttackManager.scheduleNextAttack(npcAI);
		} else {
			npcAI.setSubStateIfNot(AISubState.NONE);
			npc.clearQueuedSkills();
			npc.getGameStats().setLastSkill(null);
			npc.getGameStats().resetFightStats();
			npcAI.onGeneralEvent(AIEventType.ATTACK_FINISH);
			npcAI.onGeneralEvent(npc.isAtSpawnLocation() ? AIEventType.BACK_HOME : AIEventType.NOT_AT_HOME);
		}
	}

	public static void thinkIdle(NpcAI npcAI) {
		if (npcAI.isMoveSupported() && npcAI.getOwner().isWalker())
			WalkManager.startWalking(npcAI);
		else if (shouldResetHeading(npcAI)) {
			ThreadPoolManager.getInstance().schedule(() -> {
				if (shouldResetHeading(npcAI)) {
					npcAI.getPosition().setH(npcAI.getOwner().getSpawn().getHeading());
					PacketSendUtility.broadcastPacket(npcAI.getOwner(), new SM_HEADING_UPDATE(npcAI.getOwner()));
				}
			}, 500);
		}
	}

	private static boolean shouldResetHeading(NpcAI npcAI) {
		SpawnTemplate spawn = npcAI.getOwner().getSpawn();
		return npcAI.getTarget() == null && spawn != null && !npcAI.getOwner().getMoveController().isInMove() && npcAI.getPosition().getHeading() != spawn.getHeading() && npcAI.getOwner().isAtSpawnLocation();
	}
}
