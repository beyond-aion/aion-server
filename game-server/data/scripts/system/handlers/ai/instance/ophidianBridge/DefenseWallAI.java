package ai.instance.ophidianBridge;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("defense_wall")
public class DefenseWallAI extends GeneralNpcAI {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_REWARD:
				return false;
			case CAN_RESIST_ABNORMAL:
				return true;
			default:
				return super.ask(question);
		}
	}
}
