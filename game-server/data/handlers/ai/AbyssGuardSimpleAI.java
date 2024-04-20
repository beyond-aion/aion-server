package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Rolandas, Neon
 */
@AIName("simple_abyssguard")
public class AbyssGuardSimpleAI extends AggressiveNpcAI {

	public AbyssGuardSimpleAI(Npc owner) {
		super(owner);
	}

	@Override
	protected boolean canHandleEvent(AIEventType eventType) {
		switch (eventType) {
			case CREATURE_MOVED:
				return getState() != AIState.FIGHT;
		}
		return super.canHandleEvent(eventType);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		if (creature instanceof Npc)
			checkAggro(((Npc) creature)); // custom checkAggro for npc vs npc
		else
			super.handleCreatureSee(creature); // calls CreatureEventHandler.checkAggro
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Npc)
			checkAggro(((Npc) creature)); // custom checkAggro for npc vs npc
		else
			super.handleCreatureMoved(creature); // calls CreatureEventHandler.checkAggro
	}

	@Override
	protected boolean handleGuardAgainstAttacker(Creature attacker) {
		return false;
	}

	private void checkAggro(Npc npc) {
		if (isInState(AIState.FIGHT))
			return;

		if (isInState(AIState.RETURNING))
			return;

		Npc owner = getOwner();
		if (npc.isDead() || !owner.canSee(npc))
			return;

		if (!owner.isEnemy(npc) || npc.getLevel() < 2)
			return;

		// ignore npcs which are under attack
		if (npc.getTarget() != null)
			return;

		if (!owner.getPosition().isMapRegionActive())
			return;

		if (PositionUtil.isInRange(owner, npc, owner.getAggroRange())) {
			if (GeoService.getInstance().canSee(owner, npc)) {
				if (!isInState(AIState.RETURNING))
					getOwner().getMoveController().storeStep();
				onCreatureEvent(AIEventType.CREATURE_AGGRO, npc);
			}
		}
	}
}
