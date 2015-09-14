package ai.instance.RukibukiCircusTroupe;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu
 */
@AIName("circusboxes")
public class CircusBoxesAI2 extends NpcAI2 {

	private Future<?> spawnJesterTask;

	@Override
	protected void handleSpawned() {
		spawnJester();
		super.handleSpawned();
	}

	@Override
	protected void handleDespawned() {
		cancelspawnJesterTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1501144, getObjectId(), 0, 0);
		cancelspawnJesterTask();
		super.handleDied();
	}

	private void cancelspawnJesterTask() {
		if (spawnJesterTask != null && !spawnJesterTask.isDone()) {
			spawnJesterTask.cancel(true);
		}
	}

	private void spawnJester() {
		spawnJesterTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					int count = Rnd.get(3, 5);
					for (int i = 0; i < count; i++) {
						switch (getOwner().getNpcId()) {
							case 831348:
								rndSpawn(233462);
								break;
							case 831349:
								rndSpawn(233463);
								break;
							default:
								return;
						}
						getOwner().getController().delete();
					}
				}
			}

		}, 33000);
	}

	private void rndSpawn(int npcId) {
		float direction = Rnd.get(0, 180) / 100f;
		int distance = Rnd.get(3, 8);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		spawn(npcId, p.getX() + x1, p.getY() + y1, p.getZ(), (byte) 0);
	}
}
