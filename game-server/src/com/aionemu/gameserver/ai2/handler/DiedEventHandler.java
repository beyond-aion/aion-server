package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AI2Logger;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.NpcAI2;

/**
 * @author ATracer
 */
public class DiedEventHandler {

	public static void onDie(NpcAI2 npcAI) {
		if (npcAI.isLogging()) {
			AI2Logger.info(npcAI, "onDie");
		}

		ShoutEventHandler.onDied(npcAI);

		npcAI.setStateIfNot(AIState.DIED);
		npcAI.setSubStateIfNot(AISubState.NONE);
		npcAI.getOwner().getController().loseAggro(false);
	}

}
