package instance;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 * @reworked Estrayl October 29th, 2017.
 */
@InstanceID(301110000)
public class DanuarReliquaryInstance extends GeneralInstanceHandler {

	private AtomicBoolean isCursedModorSpawned = new AtomicBoolean();
	private AtomicInteger cloneKills = new AtomicInteger();
	private ScheduledFuture<?> wipeTask;

	protected int getExitId() {
		return 730843;
	}

	protected int getTreasureBoxId() {
		return 701795;
	}

	@Override
	public void onDie(Npc npc) {
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 284379: // Idean Obscura
			case 284378: // Idean Lapilima
			case 284377: // Danuar Reliquary Novun
				despawnNpc(npc);
				if (isNullOrDead(284377) && isNullOrDead(284378) && isNullOrDead(284379) && isCursedModorSpawned.compareAndSet(false, true))
					spawn(231304, 255.98627f, 259.0136f, 241.73842f, (byte) 90); // Cursed Queen Modor
				break;
			case 284383: // Modor's Clone
				despawnNpcs(284384); // Modor's Clone - Fake
				despawnNpc(npc);
				if (cloneKills.incrementAndGet() >= 3)
					spawn(231305, 255.98627f, 259.0136f, 241.73842f, (byte) 90);
				else
					spawnClone();
				break;
			case 231305: // Enraged Queen Modor
				onInstanceEnd(true);
				break;
			case 701795: // Treasure Box
				break;
			default:
				despawnNpc(npc);
				break;
		}
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc) {
			switch (((Npc) object).getNpcId()) {
				case 231304:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_INDER_RUNE_START());
					scheduleWipe(0);
					break;
			}
		}
	}

	private void spawnClone() {
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
	}

	private void onInstanceEnd(boolean successful) {
		cancelWipeTask();
		instance.forEachNpc(npc -> npc.getController().delete());
		spawn(getExitId(), 255.66669f, 263.78525f, 241.7986f, (byte) 86); // Spawn exit portal
		if (successful)
			spawn(getTreasureBoxId(), 256.65f, 258.09f, 241.78f, (byte) 100); // Treasure Box
	}

	private void scheduleWipe(int iterations) {
		wipeTask = ThreadPoolManager.getInstance().schedule(() -> {
			switch (iterations) {
				case 1:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_INDER_RUNE_10MIN());
					break;
				case 2:
					spawn(284387, 256.60f, 257.99f, 241.78f, (byte) 0);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_INDER_RUNE_END());
					onInstanceEnd(false);
					return;
			}
			scheduleWipe(iterations + 1);
		}, 5 * 60000);
	}

	private void cancelWipeTask() {
		if (wipeTask != null && !wipeTask.isCancelled())
			wipeTask.cancel(false);
	}

	private void despawnNpcs(int npcId) {
		for (Npc npc : instance.getNpcs(npcId))
			despawnNpc(npc);
	}

	private void despawnNpc(Npc npc) {
		if (npc != null)
			npc.getController().delete();
	}

	private boolean isNullOrDead(int npcId) {
		return getNpc(npcId) == null || getNpc(npcId).isDead();
	}

	@Override
	public void onPlayerLogOut(Player player) {
		super.onPlayerLogOut(player);
		if (player.isDead())
			TeleportService.moveToBindLocation(player);
	}

	@Override
	public void onInstanceDestroy() {
		cancelWipeTask();
	}
}
