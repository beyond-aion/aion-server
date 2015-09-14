package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.shedule.MonsterRaidSchedule;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidLocation;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.monsterraid.MonsterRaid;
import com.aionemu.gameserver.services.monsterraid.MonsterRaidStartRunnable;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Whoop
 */
public class MonsterRaidService {

	private static final Logger log = LoggerFactory.getLogger(MonsterRaidService.class);

	private static final MonsterRaidService instance = new MonsterRaidService();

	private MonsterRaidSchedule schedule;
	private final Map<Integer, MonsterRaid> activeRaids = new ConcurrentHashMap<>();
	private Map<Integer, MonsterRaidLocation> monsterRaids;

	public void initMonsterRaidLocations() {
		if (EventsConfig.ENABLE_MONSTER_RAID) {
			log.info("Initializing monster raids...");

			if (schedule != null) {
				log.error("MonsterRaidService should not be initialized two times!");
				return;
			}

			// initialize current raid locations
			monsterRaids = DataManager.RAID_DATA.getRaidLocations();
		} else {
			monsterRaids = Collections.emptyMap();
			log.info("Monster Raids are disabled in config.");
		}
	}

	public void initMonsterRaids() {
		if (!EventsConfig.ENABLE_MONSTER_RAID)
			return;
		// Initialize Raid Schedules
		schedule = MonsterRaidSchedule.load();

		for (final MonsterRaidSchedule.Raid r : schedule.getMonsterRaidsList()) {
			for (String raidTime : r.getRaidTimes()) {
				CronService.getInstance().schedule(new MonsterRaidStartRunnable(r.getRaidId()), raidTime);
				log.debug("Scheduled siege of fortressID " + r.getRaidId() + " based on cron expression: " + raidTime);
			}
		}
	}

	public void checkRaidStart(final int locationId) {
		if (getMonsterRaidLocation(locationId) != null)
			startRaid(locationId);
	}

	public void startRaid(final int monsterRaidLocationId) {
		log.debug("Starting monster raid of raid location: " + monsterRaidLocationId);

		MonsterRaid raid;
		synchronized (this) {
			if (activeRaids.containsKey(monsterRaidLocationId)) {
				log.error("Attempt to start monster raid twice for raid location: " + monsterRaidLocationId);
				return;
			}
			raid = newMonsterRaid(monsterRaidLocationId);
			activeRaids.put(monsterRaidLocationId, raid);
		}
		broadcastUpdate(raid.getMonsterRaidLocationId(), 1402383);
		raid.startMonsterRaid();
	}

	public void scheduleStop(int locId) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				stopRaid(locId);
			}

		}, 60 * 60000); // invasion ends after 1hour
	}

	public void stopRaid(final int monsterRaidLocationId) {
		log.debug("Stopping monster raid of raid location: " + monsterRaidLocationId);

		if (!isRaidInProgress(monsterRaidLocationId)) {
			log.debug("Monster Raid of raid location " + monsterRaidLocationId + " is not in progress, it was cleared earlier?");
			return;
		}

		MonsterRaid raid;
		synchronized (this) {
			raid = activeRaids.remove(monsterRaidLocationId);
		}
		if (raid == null || raid.isFinished())
			return;

		raid.stopMonsterRaid();
	}

	public Map<Integer, MonsterRaidLocation> getMonsterRaidLocations() {
		return monsterRaids;
	}

	public MonsterRaidLocation getMonsterRaidLocation(int id) {
		return monsterRaids.get(id);
	}

	protected MonsterRaid newMonsterRaid(int monsterRaidLocationId) {
		if (monsterRaids.containsKey(monsterRaidLocationId))
			return new MonsterRaid(monsterRaids.get(monsterRaidLocationId));
		else {
			log.error("Unknown monster raid handler for raid location: " + monsterRaidLocationId);
			return null;
		}
	}

	public boolean isRaidInProgress(int raidLocationId) {
		return activeRaids.containsKey(raidLocationId);
	}

	public List<Npc> spawnLocation(MonsterRaidLocation mrl) {
		List<Npc> location = new ArrayList<>();
		SpawnTemplate flagTemp = SpawnEngine.addNewSingleTimeSpawn(mrl.getWorldId(), 832819, mrl.getX(), mrl.getY(), mrl.getZ(), mrl.getH());
		SpawnTemplate vortexTemp = SpawnEngine.addNewSingleTimeSpawn(mrl.getWorldId(), 702550, mrl.getX(), mrl.getY(), mrl.getZ() + 40f, mrl.getH());
		location.add((Npc) SpawnEngine.spawnObject(flagTemp, 1));
		location.add((Npc) SpawnEngine.spawnObject(vortexTemp, 1));
		return location;
	}

	public Npc spawnBoss(MonsterRaidLocation mrl) {
		SpawnTemplate temp = SpawnEngine.addNewSingleTimeSpawn(mrl.getWorldId(), mrl.getNpcIds().get(Rnd.get(0, mrl.getNpcIds().size() - 1)), mrl.getX(),
			mrl.getY(), mrl.getZ(), mrl.getH());
		return (Npc) SpawnEngine.spawnObject(temp, 1);
	}

	public void despawnLocation(MonsterRaid mr) {
		Npc boss = mr.getBoss();
		Npc flag = mr.getFlag();
		Npc vortex = mr.getVortex();

		if (boss != null && !boss.getLifeStats().isAlreadyDead())
			boss.getController().onDelete();
		if (vortex != null && vortex.isSpawned())
			vortex.getController().onDelete();
		if (flag != null && flag.isSpawned())
			flag.getController().onDelete();
	}

	public void broadcastUpdate(final int locId, int msg) {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (activeRaids.containsKey(locId)) {
					player.getController().updateNearbyQuests();
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msg));
				}
			}
		});
	}

	public static MonsterRaidService getInstance() {
		return instance;
	}
}
