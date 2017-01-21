package com.aionemu.gameserver.ai.manager;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public class FollowManager {

	public static void targetTooFar(NpcAI npcAI) {
		Npc npc = npcAI.getOwner();
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "Follow manager - targetTooFar");
		}
		if (npcAI.isMoveSupported()) {
			npc.getMoveController().moveToTargetObject();
		}
	}
}
