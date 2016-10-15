package ai.quests;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("Q20060")
public class GarnonQ20060AI2 extends NpcAI2 {

	private Future<?> task;

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (task != null && !task.isDone())
			task.cancel(true);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		despawn();
	}

	private void despawn() {
		task = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				spawn(800020, 442.279f, 464.349f, 341.520f, (byte) 20);
				getOwner().getController().delete();
			}
		}, 60000 * 3);
	}
}
