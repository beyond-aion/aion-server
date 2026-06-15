package instance;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Ritsu, Estrayl, Yeats
 */
@InstanceID(301110000)
public class DanuarReliquaryInstance extends GeneralInstanceHandler {

	private final AtomicBoolean isCursedModorActive = new AtomicBoolean();
	private final AtomicInteger cloneKills = new AtomicInteger();
	private ScheduledFuture<?> wipeTask;

	public DanuarReliquaryInstance(WorldMapInstance instance) {
		super(instance);
	}

	protected int getExitId() {
		return 730843;
	}

	protected int getTreasureBoxId() {
		return 701795;
	}

	protected int getEnragedModorId() {
		return 231305;
	}

	protected int getCursedModorId() {
		return 231304;
	}

	protected int getRealCloneId() {
		return 284383; // 855244
	}

	protected int getFakeCloneId() {
		return 284384; // 855244
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		final int npcId = npc.getNpcId();
		switch (npcId) {
			case 284377: // Idean Obscura
			case 284378: // Idean Lapilima
			case 284379: // Danuar Reliquary Novun
				npc.getController().delete();
				if (instance.getNpcs(284377, 284378, 284379).stream().allMatch(Creature::isDead) && isCursedModorActive.compareAndSet(false, true)) {
					spawn(getCursedModorId(), 256.62f, 257.79f, 241.79f, (byte) 90);
					Npc cursedModor = getNpc(getCursedModorId());
					if (cursedModor != null) {
						// SkillEngine.getInstance().getSkill(cursedModor, 21168, 1, cursedModor).useWithoutPropSkill();
						// sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_INDER_RUNE_START());
						scheduleWipe();
					}
				}
				break;
			case 284383: // Modor's Clone
			case 855244: // Modor's Clone
				deleteAliveNpcs(getFakeCloneId());
				npc.getController().delete();
				ThreadPoolManager.getInstance().schedule(() -> {
					if (cloneKills.incrementAndGet() >= 3) {
						spawn(getEnragedModorId(), 256.62f, 257.79f, 241.79f, (byte) 90);
					} else {
						spawnClones();
					}
				}, 2000);
				break;
			case 231305: // Enraged Queen Modor
			case 234691: // Crazed Modor
				onInstanceEnd(true);
				break;
			case 701795: // Treasure Box
			case 802183:
				break;
			default:
				npc.getController().delete();
				break;
		}
	}

	private void spawnClones() {
		int spawnCase = Rnd.get(1, 5);
		switch (spawnCase) {
			case 1 -> spawn(getRealCloneId(), 255.5489f, 293.42154f, 253.78925f, (byte) 90);
			case 2 -> spawn(getRealCloneId(), 232.5363f, 263.90112f, 248.65384f, (byte) 114);
			case 3 -> spawn(getRealCloneId(), 240.11194f, 235.08876f, 251.14906f, (byte) 17);
			case 4 -> spawn(getRealCloneId(), 271.23627f, 230.30913f, 250.92981f, (byte) 42);
			case 5 -> spawn(getRealCloneId(), 284.6919f, 262.7201f, 248.75252f, (byte) 63);
		}

		if (spawnCase != 1)
			spawn(getFakeCloneId(), 255.5489f, 293.42154f, 253.78925f, (byte) 90);
		if (spawnCase != 2)
			spawn(getFakeCloneId(), 232.5363f, 263.90112f, 248.65384f, (byte) 114);
		if (spawnCase != 3)
			spawn(getFakeCloneId(), 240.11194f, 235.08876f, 251.14906f, (byte) 17);
		if (spawnCase != 4)
			spawn(getFakeCloneId(), 271.23627f, 230.30913f, 250.92981f, (byte) 42);
		if (spawnCase != 5)
			spawn(getFakeCloneId(), 284.6919f, 262.7201f, 248.75252f, (byte) 63);
	}

	protected void onInstanceEnd(boolean successful) {
		cancelWipeTask();
		Npc modor = getNpc(getEnragedModorId());
		if (modor == null) {
			modor = getNpc(getCursedModorId());
		}
		if (modor != null) {
			if (successful) {
				PacketSendUtility.broadcastMessage(modor, 343629);
			} else {
				PacketSendUtility.broadcastMessage(modor, 1500739);
			}
		}
		instance.forEachNpc(npc -> npc.getController().delete());
		spawn(getExitId(), 255.66669f, 263.78525f, 241.7986f, (byte) 86); // Spawn exit portal
		if (successful)
			spawn(getTreasureBoxId(), 256.65f, 258.09f, 241.78f, (byte) 100); // Treasure Box
	}

	private void scheduleWipe() {
		wipeTask = ThreadPoolManager.getInstance().schedule(() -> {
			spawn(284386, 256.60f, 257.99f, 241.78f, (byte) 0);
			onInstanceEnd(false);
		}, 15 * 60000);
	}

	void cancelWipeTask() {
		if (wipeTask != null && !wipeTask.isCancelled())
			wipeTask.cancel(false);
	}

	@Override
	public void onPlayerLogout(Player player) {
		super.onPlayerLogout(player);
		if (player.isDead())
			TeleportService.moveToBindLocation(player);
	}

	@Override
	public void onInstanceDestroy() {
		cancelWipeTask();
	}

	@Override
	public void onBackHome(Npc npc) {
		if (npc.getNpcId() == getEnragedModorId() || npc.getNpcId() == getCursedModorId()) {
			instance.forEachNpc(other -> {
				if (other.getNpcId() != npc.getNpcId()) {
					other.getController().delete();
				}
			});
		}
	}
}
