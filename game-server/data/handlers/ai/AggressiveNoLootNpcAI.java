package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Estrayl
 */
@AIName("aggressive_no_loot")
public class AggressiveNoLootNpcAI extends AggressiveNpcAI {

	public AggressiveNoLootNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, ALLOW_DECAY -> false;
			default -> super.ask(question);
		};
	}
}
