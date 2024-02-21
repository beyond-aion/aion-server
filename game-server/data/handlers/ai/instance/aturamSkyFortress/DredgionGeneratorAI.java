package ai.instance.aturamSkyFortress;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.GeneralNpcAI;

/**
 * @author Tibald :)
 */
@AIName("dredgion_generator")
public class DredgionGeneratorAI extends GeneralNpcAI {

	public DredgionGeneratorAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case RESIST_ABNORMAL -> true;
			default -> super.ask(question);
		};
	}
}
