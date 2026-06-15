package com.aionemu.gameserver.ai.handler;

import java.util.List;

import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.DispelSlotType;

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
		if (!npcAI.isMoveSupported()) {
			npcAI.onGeneralEvent(AIEventType.BACK_HOME);
		} else if (npcAI.setStateIfNot(AIState.RETURNING)) {
			npcAI.setSubStateIfNot(AISubState.NONE);
			if (npcAI.isLogging()) {
				AILogger.info(npcAI, "returning and restoring");
			}
			Npc npc = npcAI.getOwner();
			EmoteManager.emoteStartReturning(npc);
			if (npc.isPathWalker() && WalkManager.startWalking(npcAI))
				return;
			npc.getMoveController().returnToLastStepOrSpawn();
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
			npcAI.setSubStateIfNot(AISubState.NONE);
			npcAI.getOwner().getEffectController().removeByDispelSlotType(DispelSlotType.BUFF);
			EmoteManager.emoteStartIdling(npcAI.getOwner());
			npcAI.think();
			Npc npc = npcAI.getOwner();
			List<NpcSkillEntry> skills = npc.getSkillList().getPostSpawnSkills();
			if (!skills.isEmpty())
				skills.forEach(s -> SkillEngine.getInstance().getSkill(npc, s.getSkillId(), s.getSkillLevel(), npc).useWithoutPropSkill());
		}
		npcAI.getOwner().getPosition().getWorldMapInstance().getInstanceHandler().onBackHome(npcAI.getOwner());
	}
}
