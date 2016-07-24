package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
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

		if (!owner.isSpawned())
			return;

		if (!owner.getActiveRegion().isMapRegionActive()) // must be spawned first in order to have a valid mapregion
			return;

		if (MathUtil.isIn3dRange(owner, creature, owner.getAggroRange())) {
			ai.handleCreatureDetected(creature); // TODO: Move to AIEventType, prevent calling multiple times
			if (TribeRelationService.isAggressive(owner, creature) && (creature instanceof Player || creature.isEnemyFrom(owner))) { // aggressive mob
				if (validateAggro(owner, creature) && GeoService.getInstance().canSee(owner, creature)) {
					ShoutEventHandler.onSee(ai, creature);
					if (!ai.isInState(AIState.RETURNING))
						owner.getMoveController().storeStep();
					if (ai.canThink())
						ai.onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
				}
			} else { // non aggressive (should we consider also using geo canSee checks here?)
				ShoutEventHandler.onSee(ai, creature);
			}
		}
	}

	private static boolean validateAggro(Npc owner, Creature creature) {
		return creature.getLevel() - owner.getLevel() < 10 || owner.getObjectTemplate().getNpcTemplateType() == NpcTemplateType.ABYSS_GUARD;
	}
}
