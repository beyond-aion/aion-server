package com.aionemu.gameserver.ai2.manager;

import com.aionemu.gameserver.ai2.AI2Logger;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
public class FollowManager {

	public static void targetTooFar(NpcAI2 npcAI) {
		Npc npc = npcAI.getOwner();
		if (npcAI.isLogging()) {
			AI2Logger.info(npcAI, "Follow manager - targetTooFar");
		}
		if (npcAI.isMoveSupported()) {
			npc.getMoveController().moveToTargetObject();
		}
	}
}
