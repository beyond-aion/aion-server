package com.aionemu.gameserver.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.schedule.WorldRaidSchedules;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.worldraid.WorldRaidLocation;
import com.aionemu.gameserver.services.worldraid.WorldRaid;
import com.aionemu.gameserver.services.worldraid.WorldRaidRunnable;

/**
 * @author Whoop, Sykra
 */
public class WorldRaidService {

	private static final Logger log = LoggerFactory.getLogger(WorldRaidService.class);
	private static final WorldRaidService instance = new WorldRaidService();

	private Map<Integer, WorldRaidLocation> raidLocationsById;
	private final Map<Integer, WorldRaid> activeRaids = new ConcurrentHashMap<>();
	private final Map<Integer, Long> lastMsgDateByMapId = new ConcurrentHashMap<>();

	private WorldRaidService() {

	}

	public final void initWorldRaidLocations() {
		log.debug("Initializing world raid locations...");
		if (EventsConfig.ENABLE_WORLDRAID)
			raidLocationsById = DataManager.WORLD_RAID_DATA.getLocations();
		else
			raidLocationsById = Collections.emptyMap();
		log.debug("Finished initialization of world raid locations with size " + raidLocationsById.size());
	}

	public final void initWorldRaids() {
		log.debug("Initializing world raid schedules...");
		if (!EventsConfig.ENABLE_WORLDRAID)
			return;
		// Initialize Raid Schedules
		WorldRaidSchedules.load().getWorldRaidSchedules().forEach(worldRaidSchedule -> {
			worldRaidSchedule.getRaidTimes().forEach(cronExpr -> {
				CronService.getInstance().schedule(new WorldRaidRunnable(worldRaidSchedule), cronExpr);
			});
		});
		log.debug("Finished initialization of world raid schedules.");
	}

	public final List<WorldRaidLocation> getActiveWorldRaidLocations() {
		return activeRaids.values().stream().map(worldRaid -> raidLocationsById.get(worldRaid.getLocationId())).collect(Collectors.toList());
	}

	public final boolean isValidWorldRaidLocation(int locationId) {
		if (raidLocationsById.containsKey(locationId))
			return true;
		log.debug("No world raid location found for id: " + locationId);
		return false;
	}

	public final boolean isWorldRaidInProgress(int locationId) {
		return activeRaids.containsKey(locationId);
	}

	public final void startRaid(int locationId, boolean useSpecialSpawnMsg) {
		log.debug("Starting world raid for location: " + locationId);
		WorldRaid worldRaid;
		synchronized (this) {
			if (!isValidWorldRaidLocation(locationId)) {
				log.error("Attempt to start world raid for an invalid location: " + locationId);
				return;
			}
			if (isWorldRaidInProgress(locationId)) {
				log.error("Attempt to start world raid twice for location: " + locationId);
				return;
			}
			boolean sendMessages = true;
			WorldRaidLocation location = raidLocationsById.get(locationId);
			long currentMillis = System.currentTimeMillis();
			if (lastMsgDateByMapId.containsKey(location.getMapId())) {
				long lastMsgDateMs = lastMsgDateByMapId.get(location.getMapId());
				// 30 seconds until another message is allowed to be displayed
				if (lastMsgDateMs + 30000 < currentMillis)
					lastMsgDateByMapId.put(location.getMapId(), currentMillis);
				else
					sendMessages = false;
			} else {
				lastMsgDateByMapId.put(location.getMapId(), currentMillis);
			}
			worldRaid = new WorldRaid(location, useSpecialSpawnMsg, sendMessages);
			activeRaids.put(locationId, worldRaid);
		}
		worldRaid.startWorldRaid();
		log.debug("Finished world raid start for location: " + locationId);
	}

	public final void stopRaid(int locationId) {
		log.debug("Stopping world for location: " + locationId);
		WorldRaid raid;
		synchronized (this) {
			raid = activeRaids.remove(locationId);
		}
		if (raid == null || raid.isFinished()) {
			log.debug("Attempt to stop world raid twice for location: " + locationId);
			return;
		}
		raid.stopWorldRaid();
		log.debug("Succeeded to finish world raid for location: " + locationId);
	}

	public static WorldRaidService getInstance() {
		return instance;
	}

}
