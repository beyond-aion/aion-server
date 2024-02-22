package ai.instance.eternalBastion;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_aggressive")
public class EternalBastionAggressiveNpcAI extends AggressiveNpcAI {

	public EternalBastionAggressiveNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		if (creature instanceof Npc n && n.getNpcId() == 284685) // Work-around to not aggro the shatter mine
			return;
		super.handleCreatureAggro(creature);
	}
}
