package ai.instance.eternalBastion;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.SummonerAI;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_summoner")
public class EternalBastionSummonerAI extends SummonerAI {

	public EternalBastionSummonerAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
