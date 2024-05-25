package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Estrayl
 */
@AIName("drakenspire_seal_breaker")
public class SealBreakerAI extends AggressiveNoLootNpcAI {

	public SealBreakerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		PacketSendUtility.broadcastMessage(getOwner(), 1501334); // Arrrgh!
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		if (question == AIQuestion.IS_IMMUNE_TO_ABNORMAL_STATES)
			return true;
		return super.ask(question);
	}
}
