package instance.abyss;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_IDABRE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * A basic handler for all versions of the siege-keep instances in the inner upper abyss. <br>
 * <br>
 * Currently, the chests will spawn after the boss died and will be reduced depending on
 * the past time. Further, it is a security mechanism to avoid abuse due to geodata issues
 * handling teleportation skills. <br>
 * <br>
 * On retail all chests are spawned on instance creation and will not be deleted due to the
 * past time. At least, only the boss will be deleted after ten minutes past. <br>
 * <br>
 * Original credits to keqi, xTz, Luzien & Everlight<br>
 * <br>
 * Created on June 22nd, 2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public abstract class AbstractInnerUpperAbyssInstance extends GeneralInstanceHandler {

	/**
	 * All final spawn locations behind the end boss.
	 * Currently it is only a safety mechanism due to players can jump through doors.
	 * So it can be removed if this issue will be fixed in future.
	 */
	private List<WorldPosition> chestLocations = new ArrayList<>();

	/**
	 * For final fight trigger
	 */
	private final AtomicBoolean isFlyringPassed = new AtomicBoolean();

	/**
	 * The instance will start in hard mode and can be switched to an easier version due the killing
	 * of the artifact in the room in front of the end boss (Only available in legion and level 40+ version).
	 */
	private final AtomicBoolean isEasyMode = new AtomicBoolean();

	/**
	 * Needs to count passed time to reduce chest spawns. This is currently a custom feature to bring difficulty.
	 * On retail it is not necessary to be fast to get all chests.
	 */
	private final AtomicInteger failCounter = new AtomicInteger();

	private Future<?> chestReductionTask;

	public AbstractInnerUpperAbyssInstance(WorldMapInstance instance) {
		super(instance);
	}

	/**
	 * @return
	 *         - the awakened boss id
	 */
	protected abstract int getBossId();

	/**
	 * @return
	 *         - the ordinary chest id
	 */
	protected abstract int getChestId();

	/**
	 * @return
	 *         - the id of the door NPC
	 */
	protected abstract int getDoorId();

	/**
	 * @return
	 *         - the smallest id of the four key masters
	 */
	protected abstract int getKeymasterId();

	/**
	 * @return
	 *         - the id of the invisible NPC which is used to trigger the final timer event
	 */
	protected abstract int getTimerNpcId();

	@Override
	public void onInstanceCreate() {
		spawn(getKeymasterId() + Rnd.nextInt(4), 527.64f, 212.0511f, 178.4134f, (byte) 90);

		chestLocations.add(new WorldPosition(mapId, 575.664f, 853.248f, 199.3737f, (byte) 63));
		chestLocations.add(new WorldPosition(mapId, 571.560f, 869.936f, 199.3737f, (byte) 69));
		chestLocations.add(new WorldPosition(mapId, 560.082f, 882.979f, 199.3737f, (byte) 76));
		chestLocations.add(new WorldPosition(mapId, 545.404f, 892.116f, 199.3737f, (byte) 83));
		chestLocations.add(new WorldPosition(mapId, 528.269f, 895.106f, 199.3737f, (byte) 89));
		chestLocations.add(new WorldPosition(mapId, 511.364f, 891.941f, 199.3737f, (byte) 96));
		chestLocations.add(new WorldPosition(mapId, 496.222f, 883.099f, 199.3737f, (byte) 103));
		chestLocations.add(new WorldPosition(mapId, 484.933f, 869.897f, 199.3747f, (byte) 109));
		chestLocations.add(new WorldPosition(mapId, 479.122f, 853.576f, 199.3727f, (byte) 116));
		chestLocations.add(new WorldPosition(mapId, 478.966f, 836.407f, 199.3737f, (byte) 2));
		chestLocations.add(new WorldPosition(mapId, 485.365f, 820.350f, 199.4596f, (byte) 9));
		chestLocations.add(new WorldPosition(mapId, 576.463f, 837.337f, 199.7000f, (byte) 56)); // Bonus Chest
	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {
		if (detector.getNpcId() == getTimerNpcId() && detected instanceof Player && isFlyringPassed.compareAndSet(false, true)) {
			chestReductionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				if (failCounter.incrementAndGet() >= 11) {
					deleteAliveNpcs(getBossId());
					if (chestReductionTask != null && !chestReductionTask.isCancelled())
						chestReductionTask.cancel(true);
				}
			}, 300000, 30000);

			PacketSendUtility.broadcastToMap(instance, STR_MSG_INSTANCE_START_IDABRE());
			PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, 600));
			detector.getController().delete();
		}
	}

	/**
	 * Called to delete static door NPCs.
	 * 
	 * @param staticId
	 *          - of the door NPC to be deleted
	 */
	protected final void openDoor(int staticId) {
		instance.getNpcs(getDoorId()).stream().filter(n -> n.getSpawn().getStaticId() == staticId).forEach(n -> n.getController().delete());
	}

	/**
	 * Called after destruction of the artifact to replace the boss with his weaker version.
	 */
	protected final void switchToEasyMode() {
		if (isEasyMode.compareAndSet(false, true)) {
			Npc boss = getNpc(getBossId());
			if (boss != null && !boss.isDead()) {
				spawn(getBossId() - 1, boss.getX(), boss.getY(), boss.getZ(), boss.getHeading());
				boss.getController().delete();
			}
		}
	}

	/**
	 * Called after death of the end boss to interrupts the time task
	 * and spawns the reward chests depending on the past time.
	 */
	protected final void spawnChests() {
		if (chestReductionTask != null && !chestReductionTask.isCancelled())
			chestReductionTask.cancel(true);

		int chestCount = 11 - failCounter.get();
		for (int i = 0; i < chestCount; i++) {
			WorldPosition chestLoc = chestLocations.get(i);
			spawn(getChestId(), chestLoc.getX(), chestLoc.getY(), chestLoc.getZ(), chestLoc.getHeading());
		}
		if (!isEasyMode.get()) {
			WorldPosition bChestLoc = chestLocations.get(chestLocations.size() - 1);
			spawn(getChestId() + 1, bChestLoc.getX(), bChestLoc.getY(), bChestLoc.getZ(), bChestLoc.getHeading());
		}
		chestLocations.clear();
		chestLocations = null;
	}

	/**
	 * Called after destruction of statues to distribute a certain amount of ap.
	 * 
	 * @param ap
	 *          - the amount of abyss points the destroyed statue should reward
	 */
	protected void rewardStatueKill(int ap) {
		int rewardAp = ap / instance.getPlayerCount();
		instance.forEachPlayer(p -> AbyssPointsService.addAp(p, rewardAp));
	}

	@Override
	public boolean isBoss(Npc npc) {
		return npc.getNpcId() == getBossId();
	}
}
