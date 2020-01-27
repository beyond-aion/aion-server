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
		log.debug("Attempting to start world raid with id: " + worldRaidSchedule.getId() + " using the following location pool: "
			+ worldRaidSchedule.getLocations().stream().map(String::valueOf).collect(Collectors.joining(",")));

		List<Integer> validRaidLocations = worldRaidSchedule.getLocations().stream()
			.filter(locationId -> WorldRaidService.getInstance().isValidWorldRaidLocation(locationId)
				&& !WorldRaidService.getInstance().isWorldRaidInProgress(locationId))
			.collect(Collectors.toList());

		if (validRaidLocations.size() != worldRaidSchedule.getLocations().size()) {
			log.warn("Invalid world raid location count for raid with id: " + worldRaidSchedule.getId()
				+ " Some locations may be invalid due to a misconfiguration or due to currently running raids!");
			return;
		}

		// determine location count
		int spawnLocationCount;
		if (worldRaidSchedule.isSpecialRaid() || worldRaidSchedule.getMinCount() == 0)
			spawnLocationCount = worldRaidSchedule.getLocations().size();
		else if (worldRaidSchedule.getMinCount() > 0 && worldRaidSchedule.getMaxCount() == 0)
			spawnLocationCount = worldRaidSchedule.getMinCount();
		else
			spawnLocationCount = Rnd.get(worldRaidSchedule.getMinCount(), worldRaidSchedule.getMaxCount());

		// remove unused locations due to location count restriction
		if (spawnLocationCount != validRaidLocations.size()) {
			int locationCountToRemove = validRaidLocations.size() - spawnLocationCount;
			for (int i = 0; i < locationCountToRemove; i++)
				if (!validRaidLocations.isEmpty())
					validRaidLocations.remove(Rnd.get(0, validRaidLocations.size() - 1));
		}

		// start actual world raids using the remaining locations
		for (int locationId : validRaidLocations)
			WorldRaidService.getInstance().startRaid(locationId, worldRaidSchedule.isSpecialRaid());
		log.debug("Successfully started scheduled world raid with id: " + worldRaidSchedule.getId() + " at the following raid locations "
			+ validRaidLocations.stream().map(String::valueOf).collect(Collectors.joining(",")));
	}

}
