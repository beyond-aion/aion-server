package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public class ActivateEventHandler {

	public static void onActivate(NpcAI2 npcAI) {
		if (npcAI.isInState(AIState.IDLE)) {
			npcAI.getOwner().updateKnownlist();
			npcAI.think();
		}
	}

	public static void onDeactivate(NpcAI2 npcAI) {
		if (npcAI.isInState(AIState.WALKING) || npcAI.isInState(AIState.FORCED_WALKING)) {
			WalkManager.stopWalking(npcAI);
		}
		npcAI.think();
		Npc npc = npcAI.getOwner();
		npc.updateKnownlist();
		npc.getAggroList().clear();
		npc.getEffectController().removeAllEffects();
	}
}
