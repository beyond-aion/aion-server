package ai.siege;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;

/**
 * @author Source
 */
@AIName("siege_raceprotector")
public class SiegeRaceProtectorAI2 extends SiegeNpcAI2 {

	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.POSITIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.POSITIVE;
			case SHOULD_LOOT:
				return AIAnswers.POSITIVE;
			default:
				return null;
		}
	}

}
