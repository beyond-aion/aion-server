package ai.instance.stonespearReach;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.AggressiveNpcAI;
import javolution.util.FastTable;

/**
 * @author Yeats
 */
@AIName("aggressive_stonespear")
public class StonespearAggressiveNpcAI extends AggressiveNpcAI {

	private List<Integer> guardIds = new FastTable<>();

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		findGuardianStone();
	}

	private void findGuardianStone() {
		Collections.addAll(guardIds, new Integer[] { 855763, 855832, 855786, 856466, 856467, 856468 });
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
		getOwner().getController().delete();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
}
