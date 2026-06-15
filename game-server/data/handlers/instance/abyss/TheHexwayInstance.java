package instance.abyss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Bobobear, Sykra
 */
@InstanceID(300700000)
public class TheHexwayInstance extends GeneralInstanceHandler {

	private static final String MSG_TIMER_STARTED = "You have 22 minutes to kill all boss mobs located at the end of each bridge to unlock a bonus chest!";
	private static final String MSG_TIMER_NOTIFY = "You have %s left to unlock the bonus chest.";
	private static final String MSG_TIMER_EXPIRED = "Your time to unlock a bonus chest has expired!";
	private static final String MSG_BOSS_FAILED_NO_BOX = "You failed to kill a boss mob. No bonus chest will be unlocked.";
	private static final String MSG_BOSS_FAILED = "You failed to kill a boss mob.";
	private static final String MSG_SUCCESSFUL = "You have successfully completed the instance. A bonus chest was spawned.";

	private final int[] bossTimeLimitsSeconds = new int[] { 120, 210, 150, 150, 210, 120 };
	private final int[] treasureDoorIds = new int[] { 60, 61, 63, 64, 65, 66 };
	private final int[] bossNpcIds = new int[] { 219611, 286933, 219612, 219613, 219610, 219614 };
	private final int bonusChestTimeLimitSeconds = 1320;
	private final int[] notifyTimesSeconds = new int[] { 900, 600, 300, 120, 60, 30, 10, 3, 2, 1 };

	private final Future<?>[] scheduledBossDespawnTasks = new ScheduledFuture<?>[6];
	private final AtomicLongArray stageStartMillis = new AtomicLongArray(6);
	private final Map<Integer, Integer> playerStageMapping = new ConcurrentHashMap<>();

	private final AtomicBoolean bonusChestSpawnAllowed = new AtomicBoolean(true);
	private final AtomicLong bonusChestStartMillis = new AtomicLong();
	private final List<Future<?>> timerProgressMsgTasks = new ArrayList<>();
	private final AtomicInteger killedBossCount = new AtomicInteger();
	private final AtomicInteger attackedBossCount = new AtomicInteger();
	private Future<?> disableBonusChestSpawnTask;

	private final AtomicBoolean handlingSecondBoss = new AtomicBoolean();

	public TheHexwayInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		initTimerTrigger();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("HEXWAY_BONUSCHEST")) {
			if (bonusChestStartMillis.compareAndSet(0, System.currentTimeMillis())) {
				// schedule bonus chest spawn condition change
				disableBonusChestSpawnTask = ThreadPoolManager.getInstance().schedule(() -> {
					if (bonusChestSpawnAllowed.compareAndSet(true, false))
						broadcastYellowMessage(MSG_TIMER_EXPIRED);
				}, bonusChestTimeLimitSeconds * 1000);

				// send info message to all players
				broadcastYellowMessage(MSG_TIMER_STARTED);

				// schedule timer updates
				for (final int notifyTimeSecond : notifyTimesSeconds) {
					int scheduleDelaySeconds = bonusChestTimeLimitSeconds - notifyTimeSecond;
					if (scheduleDelaySeconds > 0)
						timerProgressMsgTasks
							.add(ThreadPoolManager.getInstance().schedule(() -> sendTimeStringToPlayers(notifyTimeSecond), scheduleDelaySeconds * 1000L));
				}
			}
		} else if (flyingRing.startsWith("HEXWAY_BOSS_")) {
			int bossIndex = Integer.parseInt(flyingRing.substring(12));
			if (bossIndex >= 0 && bossIndex <= 5) {
				if (stageStartMillis.compareAndSet(bossIndex, 0, System.currentTimeMillis())) {
					attackedBossCount.incrementAndGet();
					scheduleBossDespawn(bossIndex, bossTimeLimitsSeconds[bossIndex] * 1000);
				}
				long stageStartTimeMillis = stageStartMillis.get(bossIndex);
				if (stageStartTimeMillis > 0) {
					int bossTimeLimitSeconds = bossTimeLimitsSeconds[bossIndex];
					int elapsedTimeMillis = (int) (System.currentTimeMillis() - stageStartTimeMillis);
					if (elapsedTimeMillis <= bossTimeLimitSeconds * 1000 && scheduledBossDespawnTasks[bossIndex] != null) {
						// add player to stage mapping
						playerStageMapping.put(player.getObjectId(), bossIndex);
						// send quest timer to player
						int remainingTimeSeconds = bossTimeLimitSeconds - elapsedTimeMillis / 1000;
						PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, remainingTimeSeconds));
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.onLeaveInstance(player);
		playerStageMapping.remove(player.getObjectId());
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 0));
	}

	@Override
	public void onInstanceDestroy() {
		// cleanup all scheduled tasks
		for (Future<?> scheduledBossDespawn : scheduledBossDespawnTasks)
			if (scheduledBossDespawn != null && !scheduledBossDespawn.isDone())
				scheduledBossDespawn.cancel(true);

		if (disableBonusChestSpawnTask != null && !disableBonusChestSpawnTask.isDone())
			disableBonusChestSpawnTask.cancel(true);
		cancelTimeInformTasks();
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			// manager jarka
			case 219609:
				// open barricades
				deleteAliveNpcs(219617);
				break;
			case 219611:
			case 286933:
			case 219612:
			case 219613:
			case 219610:
			case 219614:
				// bridge bosses
				for (int bossIndex = 0; bossIndex < bossNpcIds.length; bossIndex++) {
					int bossNpcId = bossNpcIds[bossIndex];
					if (npc.getNpcId() == bossNpcId) {
						cancelBossDespawn(bossIndex);
						spawnBossChest(bossIndex);
						instance.setDoorState(treasureDoorIds[bossIndex], true);
						// remove quest timers for affected players
						for (int playerObjId : playerStageMapping.keySet()) {
							if (playerStageMapping.get(playerObjId) == bossIndex) {
								playerStageMapping.remove(playerObjId);
								Player player = instance.getPlayer(playerObjId);
								if (player != null)
									PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 0));
							}
						}
						// spawn bonus loot chest if all 6 boss npc are killed in the given time limits
						if (killedBossCount.incrementAndGet() == 6 && attackedBossCount.get() == 6) {
							if (bonusChestSpawnAllowed.compareAndSet(true, false)) {
								if (disableBonusChestSpawnTask != null && !disableBonusChestSpawnTask.isDone())
									disableBonusChestSpawnTask.cancel(true);
								spawnBonusChest();
							}
							cancelTimeInformTasks();
						}
						int remainingTimeSeconds = (int) ((bonusChestStartMillis.get() + bonusChestTimeLimitSeconds * 1000 - System.currentTimeMillis()) / 1000);
						if (remainingTimeSeconds > 0)
							sendTimeStringToPlayers(remainingTimeSeconds);
						break;
					}
				}
				break;
			// handle 2nd bridge boss
			case 219635:
			case 219636:
				if (handlingSecondBoss.compareAndSet(false, true)) {
					int secondNpcId = npc.getNpcId() == 219635 ? 219636 : 219635;
					Npc secondNpc = getNpc(secondNpcId);
					if (secondNpc != null) {
						int currentHpPercentage = secondNpc.getLifeStats().getHpPercentage();
						int bossId = bossNpcIds[1];
						Npc bossNpc = getNpc(bossId);
						if (bossNpc != null) {
							int percentageToDrop = 25 - currentHpPercentage;
							if (percentageToDrop > 0) {
								int dmgToApply = (int) (bossNpc.getLifeStats().getMaxHp() * (percentageToDrop * 0.8 / 100));
								ThreadPoolManager.getInstance().schedule(() -> {
									if (bossNpc.isDead() || bossNpc.getLifeStats().isAboutToDie())
										return;
									bossNpc.getController().onAttack(bossNpc, dmgToApply, null);
								}, 1000);
							}
							secondNpc.getController().delete();
						}
					}
					handlingSecondBoss.set(false);
				}
				break;
		}
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, mapId, 672.8343f, 606.2250f, 321.1900f, (byte) 0);
		return true;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (npc.getNpcId() == 700455)
			TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	private void cancelBossDespawn(int bossIndex) {
		Future<?> scheduledDespawn = scheduledBossDespawnTasks[bossIndex];
		if (scheduledDespawn != null && !scheduledDespawn.isDone())
			scheduledDespawn.cancel(true);
		scheduledBossDespawnTasks[bossIndex] = null;
	}

	private void scheduleBossDespawn(int bossIndex, int despawnDelayMillis) {
		ScheduledFuture<?> despawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			boolean chestSpawnWasAllowed = bonusChestSpawnAllowed.getAndSet(false);
			deleteAliveNpcs(bossNpcIds[bossIndex]);
			cancelTimeInformTasks();
			broadcastYellowMessage(chestSpawnWasAllowed ? MSG_BOSS_FAILED_NO_BOX : MSG_BOSS_FAILED);
		}, despawnDelayMillis);
		scheduledBossDespawnTasks[bossIndex] = despawnTask;
	}

	private void cancelTimeInformTasks() {
		if (timerProgressMsgTasks.isEmpty())
			return;
		Iterator<Future<?>> taskIterator = timerProgressMsgTasks.iterator();
		while (taskIterator.hasNext()) {
			Future<?> timerProgressMsgTask = taskIterator.next();
			if (timerProgressMsgTask != null && !timerProgressMsgTask.isDone()) {
				timerProgressMsgTask.cancel(true);
				taskIterator.remove();
			}
		}
	}

	private void spawnBonusChest() {
		broadcastYellowMessage(MSG_SUCCESSFUL);
		spawn(701664, 485.59f, 585.42f, 357f, (byte) 0);
	}

	private void spawnBossChest(int bossIndex) {
		switch (bossIndex) {
			case 0 -> {
				spawn(701663, 223.6263f, 427.2576f, 364.6045f, (byte) 117);
				spawn(701662, 223.5121f, 409.0381f, 365.0105f, (byte) 25);
			}
			case 1 -> {
				spawn(701663, 195.7458f, 494.1713f, 365.0105f, (byte) 2);
				spawn(701662, 193.7205f, 499.2636f, 365.0105f, (byte) 103);
			}
			case 2 -> {
				spawn(701663, 197.9107f, 566.2823f, 365.0105f, (byte) 91);
				spawn(701662, 181.2328f, 537.9518f, 365.0105f, (byte) 17);
			}
			case 3 -> {
				spawn(701663, 185.4841f, 630.1168f, 365.0105f, (byte) 108);
				spawn(701662, 201.0349f, 612.7051f, 364.6045f, (byte) 25);
			}
			case 4 -> {
				spawn(701663, 192.4604f, 673.4794f, 365.0105f, (byte) 13);
				spawn(701662, 213.7684f, 696.3926f, 365.0105f, (byte) 25);
			}
			case 5 -> {
				spawn(701663, 241.1405f, 754.4207f, 365.0105f, (byte) 96);
				spawn(701662, 214.3266f, 743.3648f, 365.0105f, (byte) 25);
			}
		}
	}

	private void initTimerTrigger() {
		spawnBonusChestTimerTrigger();
		spawnBossTimerTrigger(0, new Point3D(321.3199, 458.7944, 368.2764), new Point3D(317.8786, 465.5034, 361.7755),
			new Point3D(314.5425, 472.0469, 361.7755));
		spawnBossTimerTrigger(1, new Point3D(302.9791, 504.9413, 368.2764), new Point3D(300.9405, 511.9926, 361.7755),
			new Point3D(298.7013, 519.5820, 361.7755));
		spawnBossTimerTrigger(2, new Point3D(293.5730, 553.3477, 368.2764), new Point3D(292.3480, 560.7227, 361.7755),
			new Point3D(291.3300, 568.1430, 361.7755));
		spawnBossTimerTrigger(3, new Point3D(292.1579, 602.8574, 368.2764), new Point3D(292.4263, 610.3403, 361.7755),
			new Point3D(292.6229, 617.7412, 361.7755));
		spawnBossTimerTrigger(4, new Point3D(299.2268, 651.8597, 368.2764), new Point3D(300.9197, 659.2391, 361.7755),
			new Point3D(302.4053, 666.5773, 361.7755));
		spawnBossTimerTrigger(5, new Point3D(315.2073, 698.8103, 368.2764), new Point3D(317.9026, 705.6396, 361.7755),
			new Point3D(320.6522, 712.8006, 361.7755));
	}

	private void spawnBonusChestTimerTrigger() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("HEXWAY_BONUSCHEST", mapId, new Point3D(576.2102, 585.4146, 353.90677),
			new Point3D(576.2102, 585.4146, 359.90677), new Point3D(575.18384, 596.36664, 353.90677), 10), instance.getInstanceId());
		f1.spawn();
	}

	private void spawnBossTimerTrigger(int number, Point3D p1, Point3D center, Point3D p2) {
		FlyRing timerBarrier = new FlyRing(new FlyRingTemplate("HEXWAY_BOSS_" + number, mapId, center, p1, p2, 10), instance.getInstanceId());
		timerBarrier.spawn();
	}

	private void broadcastYellowMessage(String message) {
		PacketSendUtility.broadcastToMap(instance, new SM_MESSAGE(0, null, message, ChatType.BRIGHT_YELLOW_CENTER));
	}

	private void sendTimeStringToPlayers(int leftTimeSeconds) {
		if (bonusChestSpawnAllowed.get())
			broadcastYellowMessage(String.format(MSG_TIMER_NOTIFY, secondsToReadableString(leftTimeSeconds)));
	}

	private String secondsToReadableString(int remainingTimeSeconds) {
		int remainingMinutes = remainingTimeSeconds / 60;
		int remainingSeconds = remainingTimeSeconds - remainingMinutes * 60;
		String msg;
		if (remainingMinutes == 0)
			msg = String.format("%ds", remainingSeconds);
		else if (remainingSeconds == 0)
			msg = String.format("%dm", remainingMinutes);
		else
			msg = String.format("%1$dm %2$ds", remainingMinutes, remainingSeconds);
		return msg;
	}

}
