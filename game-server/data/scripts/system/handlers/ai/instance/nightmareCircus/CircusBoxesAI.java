package ai.instance.nightmareCircus;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu
 */
@AIName("circusboxes")
public class CircusBoxesAI extends NpcAI {

	private Future<?> spawnJesterTask;

	public CircusBoxesAI(Npc owner) {
		super(owner);
	}

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
		PacketSendUtility.broadcastMessage(getOwner(), 1501144);
		cancelspawnJesterTask();
		super.handleDied();
	}

	private void cancelspawnJesterTask() {
		if (spawnJesterTask != null && !spawnJesterTask.isDone()) {
			spawnJesterTask.cancel(true);
		}
	}

	private void spawnJester() {
		spawnJesterTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
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
