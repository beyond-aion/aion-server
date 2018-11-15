package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.AttackManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class CreatureEventHandler {

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void onCreatureMoved(NpcAI npcAI, Creature creature) {
		checkAggro(npcAI, creature);
		if (creature instanceof Player) {
			Player player = (Player) creature;
			QuestEngine.getInstance().onAtDistance(new QuestEnv(npcAI.getOwner(), player, 0));
		}
	}

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void onCreatureSee(NpcAI npcAI, Creature creature) {
		if (npcAI.isInSubState(AISubState.TARGET_LOST) && creature.equals(npcAI.getTarget())) { // see target again after hide end
			npcAI.setSubStateIfNot(AISubState.NONE);
			if (npcAI.isInState(AIState.FIGHT)) { // continue to attack
				AttackManager.scheduleNextAttack(npcAI);
				return;
			}
		}
		checkAggro(npcAI, creature);
		if (creature instanceof Player) {
			Player player = (Player) creature;
			QuestEngine.getInstance().onAtDistance(new QuestEnv(npcAI.getOwner(), player, 0));
		}
	}

	/**
	 * @param ai
	 * @param creature
	 */
	protected static void checkAggro(NpcAI ai, Creature creature) {
		if (ai.isInState(AIState.FIGHT))
			return;

		if (ai.isInState(AIState.RETURNING))
			return;

		if (creature.isDead())
			return;

		if (creature.isInVisualState(CreatureVisualState.BLINKING))
			return;

		Npc owner = ai.getOwner();
		if (!owner.isSpawned())
			return;

		if (!owner.canSee(creature))
			return;

		if (owner.getEffectController().isAbnormalSet(AbnormalState.SANCTUARY))
			return;

		if (!owner.getPosition().isMapRegionActive())
			return;

		if (PositionUtil.isInRange(owner, creature, owner.getAggroRange())) {
			ai.handleCreatureDetected(creature); // TODO: Move to AIEventType, prevent calling multiple times
			boolean isPlayer = creature instanceof Player;
			if (isPlayer && ((Player) creature).isInCustomState(CustomPlayerState.ENEMY_OF_ALL_NPCS)
				|| TribeRelationService.isAggressive(owner, creature) && (isPlayer || creature.isEnemyFrom(owner))) { // aggressive mob
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
		return creature.getLevel() - owner.getLevel() < 10 || owner.getObjectTemplate().getNpcTemplateType() == NpcTemplateType.GUARD
			|| owner.getObjectTemplate().getNpcTemplateType() == NpcTemplateType.ABYSS_GUARD;
	}
}
