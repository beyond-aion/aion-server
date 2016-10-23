package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;
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
	public static void onSpawn(NpcAI2 npcAI) {
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
	public static void onDespawn(NpcAI2 npcAI) {
		npcAI.setStateIfNot(AIState.DESPAWNED);
	}

	/**
	 * @param npcAI
	 */
	public static void onBeforeSpawn(NpcAI2 npcAI) {
		npcAI.getOwner().getMoveController().resetMove();
	}

}
