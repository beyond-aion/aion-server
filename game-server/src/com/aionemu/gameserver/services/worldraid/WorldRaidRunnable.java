package com.aionemu.gameserver.services.worldraid;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.schedule.WorldRaidSchedules;
import com.aionemu.gameserver.services.WorldRaidService;

/**
 * @author Whoop, Sykra
 */
public class WorldRaidRunnable implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WorldRaidRunnable.class);

	private final WorldRaidSchedules.WorldRaidSchedule worldRaidSchedule;

	public WorldRaidRunnable(WorldRaidSchedules.WorldRaidSchedule worldRaidSchedule) {
		this.worldRaidSchedule = worldRaidSchedule;
	}

	@Override
	public void run() {
		log.debug("Attempting to start world raid with ID: " + worldRaidSchedule.getId() + " and location pool: "	+ worldRaidSchedule.getLocations());

		List<Integer> validRaidLocations = worldRaidSchedule.getLocations().stream()
			.filter(locationId -> WorldRaidService.getInstance().isValidWorldRaidLocation(locationId)
				&& !WorldRaidService.getInstance().isWorldRaidInProgress(locationId))
			.collect(Collectors.toList());

		if (validRaidLocations.size() != worldRaidSchedule.getLocations().size()) {
			log.warn("Invalid world raid location count for raid with ID: " + worldRaidSchedule.getId()
				+ ". Some locations may be invalid due to a misconfiguration or due to currently running raids!");
			return;
		}

		// determine location count
		int spawnLocationCount = worldRaidSchedule.getLocations().size();
		if (worldRaidSchedule.getMinCount() > 0 && spawnLocationCount > worldRaidSchedule.getMinCount())
			spawnLocationCount = worldRaidSchedule.getMinCount();
		if (worldRaidSchedule.getMaxCount() > 0 && worldRaidSchedule.getMaxCount() > spawnLocationCount)
			spawnLocationCount = Rnd.get(spawnLocationCount, worldRaidSchedule.getMaxCount());

		// remove unused locations due to location count restriction
		while (validRaidLocations.size() > spawnLocationCount)
			validRaidLocations.remove(Rnd.nextInt(validRaidLocations.size()));

		// start actual world raids using the remaining locations
		for (int locationId : validRaidLocations)
			WorldRaidService.getInstance().startRaid(locationId, worldRaidSchedule.isSpecialRaid());
		if (!validRaidLocations.isEmpty())
			log.debug("Started scheduled world raid with ID " + worldRaidSchedule.getId() + " at the following raid locations: " + validRaidLocations);
	}

}
