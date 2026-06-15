package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.AttackManager;
import com.aionemu.gameserver.ai.manager.FollowManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

/**
 * @author ATracer
 */
public class TargetEventHandler {

	/**
	 * @param npcAI
	 */
	public static void onTargetReached(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onTargetReached");
		}

		AIState currentState = npcAI.getState();
		switch (currentState) {
			case FIGHT:
				npcAI.getOwner().getMoveController().abortMove();
				AttackManager.scheduleNextAttack(npcAI);
				break;
			case RETURNING:
				npcAI.getOwner().getMoveController().abortMove();
				if (npcAI.getOwner().isAtSpawnLocation())
					npcAI.onGeneralEvent(AIEventType.BACK_HOME);
				else {
					npcAI.setStateIfNot(AIState.IDLE);
					npcAI.onGeneralEvent(AIEventType.NOT_AT_HOME);
				}
				break;
			case FOLLOWING:
			case CONFUSE:
			case FEAR:
				npcAI.getOwner().getMoveController().abortMove();
				break;
			case WALKING:
				WalkManager.targetReached(npcAI);
				checkAggro(npcAI);
				break;
			case FORCED_WALKING:
				WalkManager.targetReached(npcAI);
				break;
		}
	}

	/**
	 * @param npcAI
	 */
	public static void onTargetTooFar(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onTargetTooFar");
		}
		switch (npcAI.getState()) {
			case FIGHT:
				AttackManager.targetTooFar(npcAI);
				break;
			case FOLLOWING:
				FollowManager.targetTooFar(npcAI);
				break;
			case CONFUSE:
			case FEAR:
				break;
			default:
				if (npcAI.isLogging()) {
					AILogger.info(npcAI, "default onTargetTooFar");
				}
		}
	}

	/**
	 * @param npcAI
	 */
	public static void onTargetGiveup(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onTargetGiveup");
		}
		VisibleObject target = npcAI.getOwner().getTarget();
		if (target != null) {
			if (npcAI.getSubState() == AISubState.TARGET_LOST)
				npcAI.setSubStateIfNot(AISubState.NONE);
			npcAI.getOwner().getAggroList().stopHating(target);
		}
		if (npcAI.isMoveSupported()) {
			npcAI.getOwner().getMoveController().abortMove();
		}
		if (!npcAI.isDead())
			npcAI.think();
	}

	/**
	 * @param npcAI
	 */
	public static void onTargetChange(NpcAI npcAI, Creature creature) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onTargetChange");
		}
		if (npcAI.isInState(AIState.FIGHT)) {
			npcAI.getOwner().setTarget(creature);
			AttackManager.scheduleNextAttack(npcAI);
		}
	}

	private static void checkAggro(NpcAI npcAI) {
		npcAI.getOwner().getKnownList().forEachObject(obj -> {
			if (obj instanceof Creature)
				CreatureEventHandler.checkAggro(npcAI, (Creature) obj);
		});
	}
}
