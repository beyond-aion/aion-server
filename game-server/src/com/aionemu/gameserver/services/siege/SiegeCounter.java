package com.aionemu.gameserver.services.siege;

import java.util.EnumMap;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.SiegeRace;

public class SiegeCounter {

	private final Map<SiegeRace, SiegeRaceCounter> siegeRaceCounters = new EnumMap<>(SiegeRace.class);

	public SiegeCounter() {
		siegeRaceCounters.put(SiegeRace.ELYOS, new SiegeRaceCounter(SiegeRace.ELYOS));
		siegeRaceCounters.put(SiegeRace.ASMODIANS, new SiegeRaceCounter(SiegeRace.ASMODIANS));
		siegeRaceCounters.put(SiegeRace.BALAUR, new SiegeRaceCounter(SiegeRace.BALAUR));
	}

	public void addDamage(Creature creature, int damage) {
		SiegeRace siegeRace;
		if (creature instanceof Player)
			siegeRace = SiegeRace.getByRace(creature.getRace());
		else if (creature instanceof SiegeNpc siegeNpc)
			siegeRace = siegeNpc.getSiegeRace();
		else
			return;

		siegeRaceCounters.get(siegeRace).addPoints(creature, damage);
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
	 * @return Counter with the highest total damage done to siege boss or fallbackRace, if no damage was done
	 */
	public SiegeRaceCounter getWinnerRaceCounter(SiegeRace fallbackRace) {
		return siegeRaceCounters.values().stream().filter(c -> c.getTotalDamage() > 0).sorted().findFirst().orElseGet(() -> getRaceCounter(fallbackRace));
	}

}
