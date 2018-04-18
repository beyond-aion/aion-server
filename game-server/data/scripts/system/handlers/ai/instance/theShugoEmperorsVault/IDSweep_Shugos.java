package ai.instance.theShugoEmperorsVault;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;

import ai.AggressiveNpcAI;

/**
 * @author Yeats
 */
@AIName("IDSweep_shugos")
public class IDSweep_Shugos extends AggressiveNpcAI {

	public IDSweep_Shugos(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		InstanceHandler handler = getOwner().getPosition().getWorldMapInstance().getInstanceHandler();
		InstanceReward<?> reward = null;
		if (handler != null) {
			reward = handler.getInstanceReward();
			if (reward != null) {
				if (reward.getInstanceProgressionType() == InstanceProgressionType.END_PROGRESS) {
					getOwner().getController().delete();
				}
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
}
