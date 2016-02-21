package ai.instance.stonespearReach;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.AggressiveNpcAI2;
import javolution.util.FastTable;

/**
 * @author Yeats
 *
 */
@AIName("aggressive_stonespear")
public class StonespearAggressiveNpcAI2 extends AggressiveNpcAI2 {

	private List<Integer> guardIds = new FastTable<>();
	
	@Override
	public void handleSpawned() {
		super.handleSpawned();
		findGuardianStone();
	}
	
	private void findGuardianStone() {
		Collections.addAll(guardIds, new Integer[] {855763, 855832, 855786, 856466, 856467, 856468});
		Creature target = null;
		for (Integer npcId : guardIds) {
			target = getOwner().getPosition().getWorldMapInstance().getNpc(npcId.intValue());
			if (target != null) {
				break;
			}
		}
		if (target != null) {
			getOwner().getAggroList().addHate(target, 3000);
			setStateIfNot(AIState.FIGHT);
			think();
		}		
	}
	
	@Override
	public void handleDied() {
		super.handleDied();
		getOwner().getController().onDelete();
	}
	
	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.POSITIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			case SHOULD_LOOT:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}
}
