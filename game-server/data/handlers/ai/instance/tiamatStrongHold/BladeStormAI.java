package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("bladestorm")
public class BladeStormAI extends NpcAI {

	private Future<?> spinTask;

	public BladeStormAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		spinTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> AIActions.useSkill(this, 20748), 0, 1000);
		ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 10000);
	}

	@Override
	public void handleDespawned() {
		spinTask.cancel(true);
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
