package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author ATracer
 */
public class SpawnEventHandler {

	/**
	 * @param npcAI
	 */
	public static void onSpawn(NpcAI npcAI) {
		if (npcAI.setStateIfNot(AIState.IDLE)) {
			npcAI.think();
			Npc npc = npcAI.getOwner();
			NpcSkillEntry skill = npc.getSkillList().getUseInSpawnedSkill();
			if (skill != null)
				SkillEngine.getInstance().getSkill(npc, skill.getSkillId(), skill.getSkillLevel(), npc).useWithoutPropSkill();
		}
	}

	/**
	 * @param npcAI
	 */
	public static void onDespawn(NpcAI npcAI) {
		npcAI.setStateIfNot(AIState.DESPAWNED);
	}

	/**
	 * @param npcAI
	 */
	public static void onBeforeSpawn(NpcAI npcAI) {
		npcAI.getOwner().getMoveController().resetMove();
	}

}
