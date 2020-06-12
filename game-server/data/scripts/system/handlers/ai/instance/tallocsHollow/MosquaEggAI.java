package ai.instance.tallocsHollow;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("mosquaegg")
public class MosquaEggAI extends AggressiveNpcAI {

	private Future<?> supraklawSpawnTask;

	public MosquaEggAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		supraklawSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			spawn(217132, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
			getOwner().getController().delete();
		}, 17000);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelSpawnTask();
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

	private void cancelSpawnTask() {
		if (supraklawSpawnTask != null && !supraklawSpawnTask.isDone()) {
			supraklawSpawnTask.cancel(true);
			supraklawSpawnTask = null;
		}
	}

}
