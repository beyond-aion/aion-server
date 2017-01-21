package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Rolandas
 */
public class SimpleAbyssGuardHandler {

	public static void onCreatureMoved(NpcAI npcAI, Creature creature) {
		checkAggro(npcAI, creature);
	}

	/**
	 * @param npcAI
	 * @param creature
	 */
	public static void onCreatureSee(NpcAI npcAI, Creature creature) {
		checkAggro(npcAI, creature);
	}

	protected static void checkAggro(NpcAI ai, Creature creature) {
		if (!(creature instanceof Npc)) {
			CreatureEventHandler.checkAggro(ai, creature);
			return;
		}

		Npc owner = ai.getOwner();
		if (creature.getLifeStats().isAlreadyDead() || !owner.canSee(creature))
			return;

		Npc npc = ((Npc) creature);
		if (!npc.isEnemy(creature) || npc.getLevel() < 2)
			return;

		// Creatures which are under attack not handled
		if (creature.getTarget() != null)
			return;

		if (!owner.getActiveRegion().isMapRegionActive())
			return;

		if (!ai.isInState(AIState.FIGHT) && (MathUtil.isIn3dRange(owner, creature, owner.getAggroRange()))) {
			if (GeoService.getInstance().canSee(owner, creature)) {
				if (!ai.isInState(AIState.RETURNING))
					ai.getOwner().getMoveController().storeStep();
				ai.onCreatureEvent(AIEventType.CREATURE_AGGRO, creature);
			}
		}
	}

}
