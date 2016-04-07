package ai.instance.aturamSkyFortress;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;

/**
 * @author Tibald :)
 */
@AIName("dredgion_generator")
public class DredgionGeneratorAI2 extends GeneralNpcAI2 {

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
