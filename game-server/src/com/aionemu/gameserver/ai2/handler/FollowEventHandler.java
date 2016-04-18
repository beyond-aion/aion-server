package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
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
	public static void follow(NpcAI2 npcAI, Creature creature) {
		if (npcAI.setStateIfNot(AIState.FOLLOWING)) {
			npcAI.getOwner().setTarget(creature);
			EmoteManager.emoteStartFollowing(npcAI.getOwner());
		}
	}

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void creatureMoved(NpcAI2 npcAI, Creature creature) {
		if (npcAI.isInState(AIState.FOLLOWING)) {
			if (npcAI.getOwner().isTargeting(creature.getObjectId()) && !creature.getLifeStats().isAlreadyDead()) {
				checkFollowTarget(npcAI, creature);
			}
		}
	}

	/**
	 * @param creature
	 */
	public static void checkFollowTarget(NpcAI2 npcAI, Creature creature) {
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
	public static void stopFollow(NpcAI2 npcAI, Creature creature) {
		if (npcAI.setStateIfNot(AIState.IDLE)) {
			npcAI.getOwner().setTarget(null);
			npcAI.getOwner().getMoveController().abortMove(false);
			AI2Actions.scheduleRespawn(npcAI);
			AI2Actions.deleteOwner(npcAI);
		}
	}
}
