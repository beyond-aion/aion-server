package ai.siege;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;

/**
 * @author Source
 */
@AIName("siege_raceprotector")
public class SiegeRaceProtectorAI2 extends SiegeNpcAI2 {

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_REWARD:
			case SHOULD_LOOT:
				return true;
			default:
				return super.ask(question);
		}
	}
}
