package ai.instance.theShugoEmperorsVault;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created by Yeats on 01.05.2016.
 */
@AIName("IDSweep_Boss")
public class IDSweep_Bosses extends IDSweep_Shugos {

	public IDSweep_Bosses(Npc owner) {
		super(owner);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}
}
