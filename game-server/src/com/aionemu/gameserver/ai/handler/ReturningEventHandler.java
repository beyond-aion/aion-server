package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
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
	public static void onNotAtHome(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onNotAtHome");
		}
		if (npcAI.setStateIfNot(AIState.RETURNING)) {
			if (npcAI.isLogging()) {
				AILogger.info(npcAI, "returning and restoring");
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
	public static void onBackHome(NpcAI npcAI) {
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "onBackHome");
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
