package instance.abyss;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
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

	private final Future<?>[] scheduledBossDespawns = new ScheduledFuture[6];
	private final AtomicLongArray countdownTimer = new AtomicLongArray(7);
	private final AtomicInteger currentCountdownIndex = new AtomicInteger(-2);
	private final AtomicBoolean bonusChestSpawnAllowed = new AtomicBoolean(true);
	private Future<?> bonusChestTask;

	private final int[] bossTimeLimits = new int[] { 130, 210, 150, 150, 210, 130 };
	private final int[] treasureDoors = new int[] { 60, 61, 63, 64, 65, 66 };
	private final int[] bossIds = new int[] { 219611, 286933, 219612, 219613, 219610, 219614 };
	private final int bonusChestTimeLimit = 1320;

	private int killedBossCount = 0;
	private int attackedBossCount = 0;

	private AtomicBoolean handlingSecondBoss = new AtomicBoolean(false);

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		initTimerTrigger();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("HEXWAY_WING_1")) {
			if (countdownTimer.compareAndSet(0, 0, System.currentTimeMillis())) {
				sendTimerToPlayers(bonusChestTimeLimit);
				// schedule bonus chest spawn condition change
				bonusChestTask = ThreadPoolManager.getInstance().schedule(() -> {
					bonusChestSpawnAllowed.set(false);
					if (currentCountdownIndex.get() == 0) {
						removeTimer();
					}
				}, bonusChestTimeLimit * 1000);
			}
		} else if (flyingRing.startsWith("HEXWAY_BOSS_")) {
			String number = flyingRing.substring(12);
			int boss = Integer.parseInt(number);
			if (boss >= 1 && boss <= 6) {
				if (countdownTimer.compareAndSet(boss, 0, System.currentTimeMillis())) {
					int timeLimit = bossTimeLimits[boss - 1];
					currentCountdownIndex.set(boss);
					attackedBossCount++;
					sendTimerToPlayers(timeLimit);
					scheduleBossDespawn(boss, boss - 1, timeLimit * 1000);
				}
			}
		}
		return false;
	}

	@Override
	public void onLeaveInstance(Player player) {
		if (player.isOnline()) {
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, -1));
		}
	}

	private void sendPacketToOnlineInstancePlayers(AionServerPacket packet) {
		instance.forEachPlayer(p -> {
			if (p.isOnline())
				PacketSendUtility.sendPacket(p, packet);
		});
	}

	private void cancelBossDespawn(int bossIndex) {
		Future<?> scheduledDespawn = scheduledBossDespawns[bossIndex];
		if (scheduledDespawn != null && !scheduledDespawn.isCancelled())
			scheduledDespawn.cancel(true);
		scheduledBossDespawns[bossIndex] = null;
	}

	private void scheduleBossDespawn(int bridgeNumber, int bossIndex, int despawnDelay) {
		ScheduledFuture<?> despawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			int bossId = bossIds[bossIndex];
			sendPacketToOnlineInstancePlayers(
				SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_DELETE_USE_COUNT_FINAL(DataManager.NPC_DATA.getNpcTemplate(bossId).getL10n()));
			despawnNpcs(getNpcs(bossId), true);
			bonusChestSpawnAllowed.set(false);
			if (currentCountdownIndex.get() == bridgeNumber) {
				currentCountdownIndex.set(-1);
				removeTimer();
			}
		}, despawnDelay);
		scheduledBossDespawns[bossIndex] = despawnTask;
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));
		return true;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			// manager jarka
			case 219609:
				// open/despawn barricades
				despawnNpcs(getNpcs(219617), false);
				break;
			case 219611:
			case 286933:
			case 219612:
			case 219613:
			case 219610:
			case 219614:
				// bridge bosses
				for (int bridgeNumber = 0; bridgeNumber < bossIds.length; bridgeNumber++) {
					int bossId = bossIds[bridgeNumber];
					if (npc.getNpcId() == bossId) {
						cancelBossDespawn(bridgeNumber);
						spawnBossLoot(bridgeNumber + 1);
						openDoor(treasureDoors[bridgeNumber]);
						killedBossCount++;
						if (attackedBossCount == 6) {
							if (killedBossCount == 6) {
								if (bonusChestSpawnAllowed.get()) {
									if (bonusChestTask != null && !bonusChestTask.isDone() && !bonusChestTask.isCancelled()) {
										bonusChestTask.cancel(false);
									}
									spawnBonusChest();
									bonusChestSpawnAllowed.set(false);
								}
							} else {
								currentCountdownIndex.set(-1);
							}
							removeTimer();
						} else {
							if (bonusChestSpawnAllowed.get()) {
								if (currentCountdownIndex.get() == bridgeNumber + 1) {
									currentCountdownIndex.set(0);
									sendRemainingTimeToPlayer(null, 0, bonusChestTimeLimit, true);
								}
							} else {
								currentCountdownIndex.set(-1);
								removeTimer();
							}
						}
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
						int bossId = bossIds[1];
						Npc bossNpc = getNpc(bossId);
						if (bossNpc != null) {
							int percentageToDrop = 25 - currentHpPercentage;
							if (percentageToDrop > 0) {
								double dmgToApply = bossNpc.getLifeStats().getMaxHp() * (percentageToDrop * 0.8 / 100);
								ThreadPoolManager.getInstance().schedule(() -> {
									if (!bossNpc.isDead())
										bossNpc.getController().onAttack(bossNpc, (int) dmgToApply, AttackStatus.NORMALHIT);
								}, 1000);
							}
							despawnNpc(secondNpc, true);
						}
					}
					handlingSecondBoss.set(false);
				}
				break;
		}
	}

	private void sendTimerToPlayers(int time) {
		sendPacketToOnlineInstancePlayers(new SM_QUEST_ACTION(0, time));
	}

	@Override
	public void onEnterInstance(Player player) {
		sendCurrentTimerToPlayer(player);
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, mapId, 672.8343f, 606.2250f, 321.1900f, (byte) 0);
		sendCurrentTimerToPlayer(player);
		return true;
	}

	private void removeTimer() {
		sendTimerToPlayers(-1);
	}

	private void sendRemainingTimeToPlayer(Player player, int timerIndex, long maxTime, boolean sendToInstancePlayers) {
		long startTime = countdownTimer.get(timerIndex);
		if (startTime > 0) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			elapsedTime /= 1000;
			if (elapsedTime < maxTime) {
				int remainingTime = (int) (maxTime - elapsedTime);
				if (sendToInstancePlayers) {
					sendTimerToPlayers(remainingTime);
				} else {
					if (player != null) {
						PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, remainingTime));
					}
				}
			}
		}
	}

	private List<Npc> getNpcs(int npcId) {
		return instance.getNpcs(npcId);
	}

	private void despawnNpcs(List<Npc> npcs, boolean ignoreDeadMobs) {
		for (Npc npc : npcs)
			despawnNpc(npc, ignoreDeadMobs);
	}

	private void despawnNpc(Npc npc, boolean ignoreDead) {
		if (npc != null)
			if (!ignoreDead || npc.isDead())
				NpcActions.delete(npc);
	}

	private void sendCurrentTimerToPlayer(Player player) {
		int index = currentCountdownIndex.get();
		if (index >= 0)
			sendRemainingTimeToPlayer(player, index, index == 0 ? bonusChestTimeLimit : bossTimeLimits[index], false);
	}

	private void spawnBonusChest() {
		sendPacketToOnlineInstancePlayers(SM_SYSTEM_MESSAGE.STR_MSG_IDAbRe_Core_NmdC_BoxSpawn());
		spawn(701664, 485.59f, 585.42f, 357f, (byte) 0);
	}

	private void spawnBossLoot(int bridgeNumber) {
		switch (bridgeNumber) {
			case 1:
				spawn(701663, 223.6263f, 427.2576f, 364.6045f, (byte) 117);
				if (Rnd.get(0, 100) <= 60) {
					spawn(701662, 223.5121f, 409.0381f, 365.0105f, (byte) 25);
				}
				break;
			case 2:
				spawn(701663, 195.7458f, 494.1713f, 365.0105f, (byte) 2);
				spawn(701662, 193.7205f, 499.2636f, 365.0105f, (byte) 103);
				break;
			case 3:
				spawn(701663, 197.9107f, 566.2823f, 365.0105f, (byte) 91);
				spawn(701662, 181.2328f, 537.9518f, 365.0105f, (byte) 17);
				break;
			case 4:
				spawn(701663, 185.4841f, 630.1168f, 365.0105f, (byte) 108);
				spawn(701662, 201.0349f, 612.7051f, 364.6045f, (byte) 25);
			case 5:
				spawn(701663, 192.4604f, 673.4794f, 365.0105f, (byte) 13);
				spawn(701662, 213.7684f, 696.3926f, 365.0105f, (byte) 25);
				break;
			case 6:
				spawn(701663, 241.1405f, 754.4207f, 365.0105f, (byte) 96);
				if (Rnd.get(0, 100) <= 60)
					spawn(701662, 214.3266f, 743.3648f, 365.0105f, (byte) 25);
				break;
		}
	}

	private void initTimerTrigger() {
		spawnBonusChestTimerTrigger();
		spawnBossTimerTrigger(1, new Point3D(313.6324, 456.5294, 363.2561), new Point3D(310.5882, 462.1104, 365.7663),
			new Point3D(307.6461, 468.3907, 368.2764));
		spawnBossTimerTrigger(2, new Point3D(295.2951, 503.8784, 363.2561), new Point3D(293.2075, 510.0642, 365.7663),
			new Point3D(290.7751, 516.5335, 368.2764));
		spawnBossTimerTrigger(3, new Point3D(285.3754, 553.6730, 363.2561), new Point3D(284.4015, 560.1709, 365.7663),
			new Point3D(283.3571, 566.7198, 368.2764));
		spawnBossTimerTrigger(4, new Point3D(284.2865, 604.4503, 363.2561), new Point3D(284.3718, 611.0146, 365.7663),
			new Point3D(284.3671, 617.4957, 368.2764));
		spawnBossTimerTrigger(5, new Point3D(292.3535, 655.3181, 363.2561), new Point3D(293.0183, 661.3437, 365.7663),
			new Point3D(294.3258, 667.3324, 368.2764));
		spawnBossTimerTrigger(6, new Point3D(308.2845, 703.0124, 363.2561), new Point3D(310.6309, 709.1545, 365.7663),
			new Point3D(312.7243, 714.9787, 368.2764));
	}

	private void spawnBonusChestTimerTrigger() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("HEXWAY_WING_1", mapId, new Point3D(576.2102, 585.4146, 353.90677),
			new Point3D(576.2102, 585.4146, 359.90677), new Point3D(575.18384, 596.36664, 353.90677), 10), instanceId);
		f1.spawn();
	}

	private void spawnBossTimerTrigger(int number, Point3D p1, Point3D center, Point3D p2) {
		FlyRing timerBarrier = new FlyRing(new FlyRingTemplate("HEXWAY_BOSS_" + number, mapId, center, p1, p2, 10), instanceId);
		timerBarrier.spawn();
	}

	private void openDoor(int doorId) {
		StaticDoor door = instance.getDoors().get(doorId);
		if (door != null)
			door.setOpen(true);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (npc.getNpcId() == 700455)
			TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}
}
