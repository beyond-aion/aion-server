package ai.instance.tallocsHollow;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz, Sykra
 */
@AIName("mosquaegg")
public class MosquaEggAI extends AggressiveNpcAI {

	private Future<?> supraklawSpawnTask;

	public MosquaEggAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		supraklawSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			spawn(217132, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
			getOwner().getController().delete();
		}, 17000);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelSpawnTask();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelSpawnTask();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, REWARD_AP_XP_DP_LOOT, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}

	private void cancelSpawnTask() {
		if (supraklawSpawnTask != null && !supraklawSpawnTask.isDone()) {
			supraklawSpawnTask.cancel(true);
			supraklawSpawnTask = null;
		}
	}

}
