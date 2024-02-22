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
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}
}
