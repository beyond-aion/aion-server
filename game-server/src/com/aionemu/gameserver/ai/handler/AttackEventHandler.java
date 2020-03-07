package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.AttackManager;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;

/**
 * @author ATracer
 */
public class AttackEventHandler {

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void onAttack(NpcAI npcAI, Creature creature) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onAttack");
		}
		if (creature == null || creature.isDead()) {
			return;
		}
		// TODO lock or better switch
		if (npcAI.isInState(AIState.RETURNING)) {
			npcAI.getOwner().getMoveController().abortMove();
			npcAI.setStateIfNot(AIState.IDLE);
			npcAI.onGeneralEvent(AIEventType.NOT_AT_HOME);
			return;
		}
		if (!npcAI.canThink()) {
			return;
		}
		if (npcAI.isInState(AIState.WALKING)) {
			WalkManager.stopWalking(npcAI);
		}
		npcAI.getOwner().getGameStats().renewLastAttackedTime();
		boolean allowFight = npcAI.getState() != AIState.FEAR && !npcAI.getOwner().getEffectController().isAbnormalSet(AbnormalState.FEAR)
				&& npcAI.getState() != AIState.CONFUSE && !npcAI.getOwner().getEffectController().isAbnormalSet(AbnormalState.CONFUSE);
		if (allowFight && npcAI.setStateIfNot(AIState.FIGHT)) {
			if (npcAI.isLogging())
				AILogger.info(npcAI, "onAttack() -> startAttacking");
			npcAI.setSubStateIfNot(AISubState.NONE);
			if (npcAI.getOwner().canSee(creature))
				npcAI.getOwner().setTarget(creature);
			AttackManager.startAttacking(npcAI);
			ShoutEventHandler.onAttackBegin(npcAI);
		}
	}

	/**
	 * @param npcAI
	 */
	public static void onForcedAttack(NpcAI npcAI) {
		onAttack(npcAI, (Creature) npcAI.getOwner().getTarget());
	}

	/**
	 * @param npcAI
	 */
	public static void onAttackComplete(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onAttackComplete: " + npcAI.getOwner().getGameStats().getLastAttackTimeDelta());
		}
		npcAI.getOwner().getGameStats().renewLastAttackTime();
		AttackManager.scheduleNextAttack(npcAI);
	}

	/**
	 * @param npcAI
	 */
	public static void onFinishAttack(NpcAI npcAI) {
		if (!npcAI.canThink()) {
			return;
		}
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onFinishAttack");
		}
		Npc npc = npcAI.getOwner();
		EmoteManager.emoteStopAttacking(npc);
		ShoutEventHandler.onAttackEnd(npcAI);
		npc.getController().loseAggro(true);
		npc.setSkillNumber(0);
	}
}
