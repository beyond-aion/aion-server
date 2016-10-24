package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AI2Logger;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author ATracer
 */
public class ReturningEventHandler {

	/**
	 * @param npcAI
	 */
	public static void onNotAtHome(NpcAI2 npcAI) {
		if (npcAI.isLogging()) {
			AI2Logger.info(npcAI, "onNotAtHome");
		}
		if (npcAI.setStateIfNot(AIState.RETURNING)) {
			if (npcAI.isLogging()) {
				AI2Logger.info(npcAI, "returning and restoring");
			}
			EmoteManager.emoteStartReturning(npcAI.getOwner());
			Npc npc = npcAI.getOwner();
			if (AIConfig.ACTIVE_NPC_MOVEMENT && npc.isPathWalker()) {
				WalkManager.startWalking(npcAI);
			} else {
				Point3D prevStep = npc.getMoveController().recallPreviousStep();
				npc.getMoveController().moveToPoint(prevStep.getX(), prevStep.getY(), prevStep.getZ());
			}
		}
	}

	/**
	 * @param npcAI
	 */
	public static void onBackHome(NpcAI2 npcAI) {
		if (npcAI.isLogging()) {
			AI2Logger.info(npcAI, "onBackHome");
		}
		npcAI.getOwner().getMoveController().clearBackSteps();
		if (npcAI.setStateIfNot(AIState.IDLE)) {
			EmoteManager.emoteStartIdling(npcAI.getOwner());
			npcAI.think();
			Npc npc = npcAI.getOwner();
			NpcSkillEntry skill = npc.getSkillList().getUseInSpawnedSkill();
			if (skill != null)
				SkillEngine.getInstance().getSkill(npc, skill.getSkillId(), skill.getSkillLevel(), npc).useWithoutPropSkill();
		}
	}
}
