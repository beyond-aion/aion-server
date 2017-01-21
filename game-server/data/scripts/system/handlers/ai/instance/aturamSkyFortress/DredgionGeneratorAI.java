package ai.instance.aturamSkyFortress;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;

import ai.GeneralNpcAI;

/**
 * @author Tibald :)
 */
@AIName("dredgion_generator")
public class DredgionGeneratorAI extends GeneralNpcAI {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case CAN_RESIST_ABNORMAL:
				return true;
			default:
				return super.ask(question);
		}
	}
}
