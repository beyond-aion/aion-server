package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Source
 */
@AIName("siege_raceprotector")
public class SiegeRaceProtectorAI extends SiegeNpcAI {

	public SiegeRaceProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, REWARD_AP_XP_DP_LOOT, REWARD_LOOT -> true;
			default -> super.ask(question);
		};
	}
}
