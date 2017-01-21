package instance;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@InstanceID(301110000)
public class DanuarReliquaryInstance extends GeneralInstanceHandler {

	private AtomicInteger cloneKill = new AtomicInteger();
	private AtomicBoolean isSpawning = new AtomicBoolean();
	private AtomicBoolean isBossSpawned = new AtomicBoolean();
	private Future<?> timer10minTask;
	private Future<?> timer15minTask;

	@Override
	public void onDie(Npc npc) {
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 284379:
			case 284378:
			case 284377:
				despawnNpc(npc);
				if (isDeadNpc(284377) && isDeadNpc(284378) && isDeadNpc(284379) && isBossSpawned.compareAndSet(false, true)) {
					spawn(231304, 255.98627f, 259.0136f, 241.73842f, (byte) 90);
					sendMsg(1401676);
					timer10minTask = ThreadPoolManager.getInstance().schedule(() -> sendMsg(1401677), 10 * 60000);
					timer15minTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							// KILLALL (skill 21199)
							sendMsg(1401678);
							despawnNpcs(231304); // despawn bosses
							despawnNpcs(231305);
							despawnAll();
							spawn(730843, 255.66669f, 263.78525f, 241.7986f, (byte) 86); // Spawn exit portal
						}
					}, 15 * 60000);
				}
				break;
			case 284383:
				despawnNpcs(284384);
				despawnNpc(npc);
				if (cloneKill.incrementAndGet() == 3)
					spawn(231305, 255.98627f, 259.0136f, 241.73842f, (byte) 90);
				else if (isSpawning.compareAndSet(false, true))
					spawnClone();
				break;
			case 231305:
				despawnAll();
				finalSpawn();
				cancelTasks();
				break;
			case 701795:
				// don't delete
				break;
			default:
				despawnNpc(npc);
				break;
		}
	}

	protected void finalSpawn() {
		spawn(730843, 255.66669f, 263.78525f, 241.7986f, (byte) 86); // Spawn exit portal
		spawn(701795, 256.65f, 258.09f, 241.78f, (byte) 100);
	}

	private void despawnAll() {
		despawnNpcs(284383);
		despawnNpcs(284384);
		despawnNpcs(284659);
		despawnNpcs(284660);
		despawnNpcs(284661);
		despawnNpcs(284662);
		despawnNpcs(284663);
		despawnNpcs(284664);
	}

	private void despawnNpcs(int npcId) {
		deleteNpcs(instance.getNpcs(npcId));
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			despawnNpc(npc);
		}
	}

	protected void despawnNpc(Npc npc) {
		if (npc != null) {
			npc.getController().delete();
		}
	}

	private void spawnClone() {
		try {
			int spawnCase = Rnd.get(1, 5);
			switch (spawnCase) {
				case 1:
					spawn(284383, 255.5489f, 293.42154f, 253.78925f, (byte) 90);
					break;
				case 2:
					spawn(284383, 232.5363f, 263.90112f, 248.65384f, (byte) 114);
					break;
				case 3:
					spawn(284383, 240.11194f, 235.08876f, 251.14906f, (byte) 17);
					break;
				case 4:
					spawn(284383, 271.23627f, 230.30913f, 250.92981f, (byte) 42);
					break;
				case 5:
					spawn(284383, 284.6919f, 262.7201f, 248.75252f, (byte) 63);
					break;
			}

			if (spawnCase != 1)
				spawn(284384, 255.5489f, 293.42154f, 253.78925f, (byte) 90);
			if (spawnCase != 2)
				spawn(284384, 232.5363f, 263.90112f, 248.65384f, (byte) 114);
			if (spawnCase != 3)
				spawn(284384, 240.11194f, 235.08876f, 251.14906f, (byte) 17);
			if (spawnCase != 4)
				spawn(284384, 271.23627f, 230.30913f, 250.92981f, (byte) 42);
			if (spawnCase != 5)
				spawn(284384, 284.6919f, 262.7201f, 248.75252f, (byte) 63);
		} finally {
			isSpawning.set(false);
		}
	}

	private boolean isDeadNpc(int npcId) {
		return (getNpc(npcId) == null || getNpc(npcId).getLifeStats().isAlreadyDead());
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks();
	}

	@Override
	public void onPlayerLogOut(Player player) {
		super.onPlayerLogOut(player);
		if (player.getLifeStats().isAlreadyDead()) {
			TeleportService.moveToBindLocation(player);
		}
	}

	private void cancelTasks() {
		if (timer10minTask != null && !timer10minTask.isDone()) {
			timer10minTask.cancel(true);
		}
		if (timer15minTask != null && !timer15minTask.isDone()) {
			timer15minTask.cancel(true);
		}
	}
}
