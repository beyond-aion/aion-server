package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public class ActivateEventHandler {

	public static void onActivate(NpcAI npcAI) {
		if (npcAI.isInState(AIState.IDLE)) {
			npcAI.getOwner().updateKnownlist();
			npcAI.think();
		}
	}

	public static void onDeactivate(NpcAI npcAI) {
		npcAI.think();
		Npc npc = npcAI.getOwner();
		npc.updateKnownlist();
		npc.getController().loseAggro(false);
		if (npcAI.ask(AIQuestion.REMOVE_EFFECTS_ON_MAP_REGION_DEACTIVATE))
			npc.getEffectController().removeAllEffects();
	}
}
