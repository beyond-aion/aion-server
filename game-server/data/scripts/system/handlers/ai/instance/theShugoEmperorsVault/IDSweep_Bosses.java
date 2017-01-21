package ai.instance.theShugoEmperorsVault;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;

/**
 * Created by Yeats on 01.05.2016.
 */
@AIName("IDSweep_Boss")
public class IDSweep_Bosses extends IDSweep_Shugos {

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_REWARD:
			case SHOULD_LOOT:
				return false;
			case CAN_RESIST_ABNORMAL:
				return true;
			default:
				return super.ask(question);
		}
	}
}
