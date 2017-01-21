package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.handler.SimpleAbyssGuardHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Rolandas
 */
@AIName("simple_abyssguard")
public class AbyssGuardSimpleAI extends AggressiveNpcAI {

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
		SimpleAbyssGuardHandler.onCreatureSee(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		SimpleAbyssGuardHandler.onCreatureMoved(this, creature);
	}

	@Override
	protected boolean handleGuardAgainstAttacker(Creature attacker) {
		return false;
	}

}
