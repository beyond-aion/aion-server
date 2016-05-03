package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Rolandas
 */
public class FreezeEventHandler {

	public static void onUnfreeze(AbstractAI ai) {
		if (ai.isInSubState(AISubState.FREEZE)) {
			ai.setSubStateIfNot(AISubState.NONE);
			if (ai instanceof NpcAI2) {
				Npc npc = ((NpcAI2) ai).getOwner();
				if (npc.getWalkerGroup() != null) {
					ai.setStateIfNot(AIState.WALKING);
					ai.setSubStateIfNot(AISubState.WALK_WAIT_GROUP);
				} else if (npc.getSpawn().getRandomWalk() > 0) {
					ai.setStateIfNot(AIState.WALKING);
					ai.setSubStateIfNot(AISubState.WALK_RANDOM);
				}
				npc.updateKnownlist();
			}
			ai.think();
		}
	}

	public static void onFreeze(AbstractAI ai) {
		if (ai.isInState(AIState.WALKING) || ai.isInState(AIState.FORCED_WALKING)) {
			WalkManager.stopWalking((NpcAI2) ai);
		}
		ai.setStateIfNot(AIState.IDLE);
		ai.setSubStateIfNot(AISubState.FREEZE);
		ai.think();
		if (ai instanceof NpcAI2) {
			Npc npc = ((NpcAI2) ai).getOwner();
			npc.updateKnownlist();
			npc.getAggroList().clear();
			npc.getEffectController().removeAllEffects();
		}
	}
}
