package ai.instance.theShugoEmperorsVault;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;

import ai.AggressiveNpcAI2;

/**
 * @author Yeats
 */
@AIName("IDSweep_shugos")
public class IDSweep_Shugos extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		InstanceHandler handler = getOwner().getPosition().getWorldMapInstance().getInstanceHandler();
		InstanceReward<?> reward = null;
		if (handler != null) {
			reward = handler.getInstanceReward();
			if (reward != null) {
				if (reward.getInstanceScoreType() == InstanceScoreType.END_PROGRESS) {
					getOwner().getController().delete();
				}
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_REWARD:
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
}
