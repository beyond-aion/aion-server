package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class CreatureEventHandler {

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void onCreatureMoved(NpcAI2 npcAI, Creature creature) {
		checkAggro(npcAI, creature);
		if (creature instanceof Player) {
			Player player = (Player) creature;
			QuestEngine.getInstance().onAtDistance(new QuestEnv(npcAI.getOwner(), player, 0, 0));
		}
	}

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void onCreatureSee(NpcAI2 npcAI, Creature creature) {
		checkAggro(npcAI, creature);
		if (creature instanceof Player) {
			Player player = (Player) creature;
			QuestEngine.getInstance().onAtDistance(new QuestEnv(npcAI.getOwner(), player, 0, 0));
		}
	}

	/**
	 * @param ai
	 * @param creature
	 */
	protected static void checkAggro(NpcAI2 ai, Creature creature) {
		Npc owner = ai.getOwner();

		if (ai.isInState(AIState.FIGHT))
			return;

		if (creature.getLifeStats().isAlreadyDead())
			return;

		if (!owner.canSee(creature))
			return;

		if (owner.getEffectController().isAbnormalState(AbnormalState.SANCTUARY))
			return;

		if (!owner.getActiveRegion().isMapRegionActive())
			return;

		boolean isInAggroRange = false;

		if (ai.poll(AIQuestion.CAN_SHOUT)) {
			int shoutRange = owner.getObjectTemplate().getMinimumShoutRange();
			double distance = MathUtil.getDistance(owner, creature);
			if (distance <= shoutRange) {
				ShoutEventHandler.onSee(ai, creature);
				isInAggroRange = shoutRange <= owner.getAggroRange();
			}
		}

		if (!ai.isInState(AIState.FIGHT) && (isInAggroRange || MathUtil.isIn3dRange(owner, creature, owner.getAggroRange()))) {
			if (checkAggroRelation(owner, creature) && GeoService.getInstance().canSee(owner, creature)) {
				if (!ai.isInState(AIState.RETURNING))
					ai.getOwner().getMoveController().storeStep();
				if (ai.canThink())
					ai.onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
			}
		}
	}

	private static boolean checkAggroRelation(Npc owner, Creature creature) {
		if (TribeRelationService.isAggressive(owner, creature) && owner.isEnemy(creature)) {
			if (creature.getLevel() - owner.getLevel() < 10 || owner.getObjectTemplate().getNpcTemplateType() == NpcTemplateType.ABYSS_GUARD) {
				return true;
			}
		}
		return false;
	}
}
