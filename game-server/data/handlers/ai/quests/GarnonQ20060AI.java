package ai.quests;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("Q20060")
public class GarnonQ20060AI extends NpcAI {

	private Future<?> task;

	public GarnonQ20060AI(Npc owner) {
		super(owner);
	}

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
