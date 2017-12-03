package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.AggroEventHandler;
import com.aionemu.gameserver.ai.handler.CreatureEventHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
@AIName("aggressive")
public class AggressiveNpcAI extends GeneralNpcAI {

	public AggressiveNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		CreatureEventHandler.onCreatureSee(this, creature);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		if (canThink())
			AggroEventHandler.onAggro(this, creature);
	}

	@Override
	protected boolean handleGuardAgainstAttacker(Creature attacker) {
		return AggroEventHandler.onGuardAgainstAttacker(this, attacker);
	}

}
