package ai.instance.argentManor;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIQuestion;

/**
 * @author xTz
 */
@AIName("eldritch_surkana")
public class EldritchSurkanaAI2 extends NpcAI2 {

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case CAN_RESIST_ABNORMAL:
				return true;
			default:
				return super.ask(question);
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		AI2Actions.deleteOwner(this);
	}

}
