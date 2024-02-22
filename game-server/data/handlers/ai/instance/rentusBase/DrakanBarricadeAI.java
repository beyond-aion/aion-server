package ai.instance.rentusBase;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.NoActionAI;

/**
 * @author Sykra
 */
@AIName("drakanbarricade")
public class DrakanBarricadeAI extends NoActionAI {

	public DrakanBarricadeAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean ask(AIQuestion question) {
		if (question == AIQuestion.REWARD_LOOT)
			return false;
		return super.ask(question);
	}

}
