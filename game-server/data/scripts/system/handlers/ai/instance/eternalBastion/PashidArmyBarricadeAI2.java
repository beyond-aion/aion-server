package ai.instance.eternalBastion;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;

import ai.GeneralNpcAI2;

/**
 * @author Tibald
 */
@AIName("pashid_army_barricade")
public class PashidArmyBarricadeAI2 extends GeneralNpcAI2 {

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
