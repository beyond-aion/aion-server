package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;

/**
 * @author ATracer
 */
public class DiedEventHandler {

	public static void onDie(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onDie");
		}

		ShoutEventHandler.onDied(npcAI);

		npcAI.setStateIfNot(AIState.DIED);
		npcAI.setSubStateIfNot(AISubState.NONE);
		npcAI.getOwner().getController().loseAggro(false);
	}

}
