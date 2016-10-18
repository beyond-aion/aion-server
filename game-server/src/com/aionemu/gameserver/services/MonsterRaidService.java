package com.aionemu.gameserver.services;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.shedule.MonsterRaidSchedule;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidLocation;
import com.aionemu.gameserver.services.monsterraid.MonsterRaid;
import com.aionemu.gameserver.services.monsterraid.MonsterRaidStartRunnable;

/**
 * @author Whoop
 */
public class MonsterRaidService {

	private static final Logger log = LoggerFactory.getLogger(MonsterRaidService.class);
	private static final MonsterRaidService instance = new MonsterRaidService();
	private final Map<Integer, MonsterRaid> activeRaids = new ConcurrentHashMap<>();
	private Map<Integer, MonsterRaidLocation> monsterRaids;

	public final void initMonsterRaidLocations() {
		log.debug("Initializing monster raid locations...");
		if (EventsConfig.ENABLE_MONSTER_RAID)
			monsterRaids = DataManager.RAID_DATA.getRaidLocations();
		else
			monsterRaids = Collections.emptyMap();
		log.debug("Finished initialization of monster raid locations with size " + monsterRaids.size());
	}

	public final void initMonsterRaids() {
		log.debug("Initializing monster raid schedules...");
		if (!EventsConfig.ENABLE_MONSTER_RAID)
			return;
		// Initialize Raid Schedules
		MonsterRaidSchedule.load().getMonsterRaidsList().stream()
			.forEach(r -> r.getRaidTimes().stream().forEach(rt -> CronService.getInstance().schedule(new MonsterRaidStartRunnable(r.getRaidId()), rt)));
		log.debug("Finished initialization of monster raid schedules.");
	}

	public final void checkRaidStart(int locId) {
		if (getMonsterRaidLocation(locId) != null)
			startRaid(locId);
		else
			log.debug("No monster raid location found for id: " + locId);
	}

	public final void startRaid(int locId) {
		log.debug("Starting monster raid of raid location: " + locId);
		MonsterRaid raid;
		if (activeRaids.containsKey(locId)) {
			log.error("Attempt to start monster raid twice for raid location: " + locId);
			return;
		}
		synchronized (this) {
			raid = newMonsterRaid(locId);
			activeRaids.put(locId, raid);
		}
		try {
			raid.startMonsterRaid();
		} catch (RuntimeException e) {
			log.error("MonsterRaid could not be started! ID:" + locId, e);
		}
		log.debug("Finished monster raid start of raid location: " + locId);
	}

	public final void stopRaid(int locId) {
		log.debug("Stopping monster raid of raid location: " + locId);
		MonsterRaid raid;
		synchronized (this) {
			raid = activeRaids.remove(locId);
		}
		if (raid == null || raid.isFinished()) {
			log.debug("Attempt to stop monster raid twice for raid location: " + locId);
			return;
		}
		try {
			raid.stopMonsterRaid();
		} catch (RuntimeException e) {
			log.error("monster raid could not be finished! ID:" + locId, e);
		}
		log.debug("Succeeded to finish monster raid of raid location: " + locId);
	}

	private MonsterRaid newMonsterRaid(int locId) throws RuntimeException {
		if (monsterRaids.containsKey(locId))
			return new MonsterRaid(monsterRaids.get(locId));
		else
			throw new RuntimeException("Tried to start same MonsterRaid twice! ID:" + locId);
	}

	public Map<Integer, MonsterRaidLocation> getMonsterRaidLocations() {
		return monsterRaids;
	}

	public MonsterRaidLocation getMonsterRaidLocation(int id) {
		return monsterRaids.get(id);
	}

	public boolean isRaidInProgress(int raidLocationId) {
		return activeRaids.containsKey(raidLocationId);
	}

	public static MonsterRaidService getInstance() {
		return instance;
	}
}
