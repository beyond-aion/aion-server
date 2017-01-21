package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AbstractAI;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author ATracer
 */
public class FollowEventHandler {

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void follow(NpcAI npcAI, Creature creature) {
		if (npcAI.setStateIfNot(AIState.FOLLOWING)) {
			npcAI.getOwner().setTarget(creature);
			EmoteManager.emoteStartFollowing(npcAI.getOwner());
		}
	}

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void creatureMoved(NpcAI npcAI, Creature creature) {
		if (npcAI.isInState(AIState.FOLLOWING)) {
			if (npcAI.getOwner().isTargeting(creature.getObjectId()) && !creature.getLifeStats().isAlreadyDead()) {
				checkFollowTarget(npcAI, creature);
			}
		}
	}

	/**
	 * @param creature
	 */
	public static void checkFollowTarget(NpcAI npcAI, Creature creature) {
		if (!isInRange(npcAI, creature)) {
			npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
		}
	}

	public static boolean isInRange(AbstractAI ai, VisibleObject object) {
		if (object == null) {
			return false;
		}
		return MathUtil.isIn3dRange(ai.getOwner(), object, 2);
	}

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void stopFollow(NpcAI npcAI, Creature creature) {
		if (npcAI.setStateIfNot(AIState.IDLE)) {
			npcAI.getOwner().setTarget(null);
			npcAI.getOwner().getMoveController().abortMove();
			AIActions.scheduleRespawn(npcAI);
			AIActions.deleteOwner(npcAI);
		}
	}
}
