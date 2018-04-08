package com.aionemu.gameserver.services.siege;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.SiegeRace;

public class SiegeCounter {

	private final Map<SiegeRace, SiegeRaceCounter> siegeRaceCounters = new ConcurrentHashMap<>();

	public SiegeCounter() {
		siegeRaceCounters.put(SiegeRace.ELYOS, new SiegeRaceCounter(SiegeRace.ELYOS));
		siegeRaceCounters.put(SiegeRace.ASMODIANS, new SiegeRaceCounter(SiegeRace.ASMODIANS));
		siegeRaceCounters.put(SiegeRace.BALAUR, new SiegeRaceCounter(SiegeRace.BALAUR));
	}

	public void addDamage(Creature creature, int damage) {
		SiegeRace siegeRace;
		if (creature instanceof Player)
			siegeRace = SiegeRace.getByRace(creature.getRace());
		else if (creature instanceof SiegeNpc) {
			siegeRace = ((SiegeNpc) creature).getSiegeRace();
			if (siegeRace == null) {
				LoggerFactory.getLogger(SiegeCounter.class).warn("Missing siegeRace for " + creature);
				return;
			}
		} else
			return;

		siegeRaceCounters.get(siegeRace).addPoints(creature, damage);
	}

	/**
	 * Clear all damage progress (on siege protector reset)
	 */
	public void clearDamageCounters() {
		getRaceCounter(SiegeRace.ELYOS).clearDamages();
		getRaceCounter(SiegeRace.ASMODIANS).clearDamages();
		getRaceCounter(SiegeRace.BALAUR).clearDamages();
	}

	public void addAbyssPoints(Player player, int ap) {
		SiegeRace sr = SiegeRace.getByRace(player.getRace());
		siegeRaceCounters.get(sr).addAbyssPoints(player, ap);
	}

	public SiegeRaceCounter getRaceCounter(SiegeRace race) {
		return siegeRaceCounters.get(race);
	}

	public void addRaceDamage(SiegeRace race, int damage) {
		getRaceCounter(race).addTotalDamage(damage);
	}

	/**
	 * Returns list of siege race counters sorted by total damage done to siege boss. Sorted in descending order.
	 *
	 * @return all siege race damage counters sorted by descending order
	 */
	public SiegeRaceCounter getWinnerRaceCounter() {
		List<SiegeRaceCounter> list = new ArrayList<>(siegeRaceCounters.values());
		list.sort(null);
		return list.get(0);
	}

}
