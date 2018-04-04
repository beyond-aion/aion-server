package com.aionemu.gameserver.model.siege;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.services.SiegeService;

/**
 * Holds and calculates the faction influences based on the fortresses template influence values (which should sum up to 100).<br/>
 * The faction who owns a fortress gets the associated influence value. Based on these values, the global influence rate is then calculated.
 *
 * @author Sarynth, Neon
 */
public class Influence {

	private static final Influence instance = new Influence();
	private Map<Integer, Map<SiegeRace, Integer>> influencesByWorld;
	private Map<SiegeRace, Integer> globalInfluences;
	private float elyosInfluenceRate;
	private float asmoInfluenceRate;
	private float balaurInfluenceRate;

	private Influence() {
		recalculateInfluence();
	}

	public static Influence getInstance() {
		return instance;
	}

	public void recalculateInfluence() {
		influencesByWorld = calculateFortressWorldInfluences();
		globalInfluences = calculateGlobalInfluences(influencesByWorld);
		elyosInfluenceRate = getInfluence(SiegeRace.ELYOS) / 100f;
		asmoInfluenceRate = getInfluence(SiegeRace.ASMODIANS) / 100f;
		balaurInfluenceRate = getInfluence(SiegeRace.BALAUR) / 100f;
	}

	private Map<Integer, Map<SiegeRace, Integer>> calculateFortressWorldInfluences() {
		Map<Integer, Map<SiegeRace, Integer>> fortressWorldInfluences = new LinkedHashMap<>();
		for (SiegeLocation sLoc : SiegeService.getInstance().getSiegeLocations().values()) {
			int influence = sLoc.getInfluenceValue();
			if (influence > 0) {
				fortressWorldInfluences.compute(sLoc.getWorldId(), (worldId, influences) -> {
					if (influences == null)
						influences = new EnumMap<>(SiegeRace.class);
					influences.compute(sLoc.getRace(), (k, value) -> value == null ? influence : value + influence);
					return influences;
				});
			}
		}
		return fortressWorldInfluences;
	}

	private Map<SiegeRace, Integer> calculateGlobalInfluences(Map<Integer, Map<SiegeRace, Integer>> influencesByWorld) {
		if (influencesByWorld.isEmpty())
			return Collections.emptyMap();
		return influencesByWorld.values().stream().flatMap(e -> e.entrySet().stream())
			.collect(Collectors.groupingBy(Map.Entry::getKey, () -> new EnumMap<>(SiegeRace.class), Collectors.summingInt(Map.Entry::getValue)));
	}

	public float getElyosInfluenceRate() {
		return elyosInfluenceRate;
	}

	public float getAsmodianInfluenceRate() {
		return asmoInfluenceRate;
	}

	public float getBalaurInfluenceRate() {
		return balaurInfluenceRate;
	}

	public int getInfluence(SiegeRace race) {
		return globalInfluences.getOrDefault(race, 0);
	}

	public int getInfluence(int worldId, SiegeRace race) {
		return influencesByWorld.getOrDefault(worldId, Collections.emptyMap()).getOrDefault(race, 0);
	}

	public Set<Integer> getInfluenceRelevantWorldIds() {
		return influencesByWorld.keySet();
	}

	/**
	 * @return float containing dmg modifier for disadvantaged race
	 */
	public float getPvpRaceBonus(Race attRace) {
		switch (attRace) {
			case ASMODIANS:
				return calculatePvpRaceBonus(getAsmodianInfluenceRate(), getElyosInfluenceRate());
			case ELYOS:
				return calculatePvpRaceBonus(getElyosInfluenceRate(), getAsmodianInfluenceRate());
			default:
				return 1f;
		}
	}

	private float calculatePvpRaceBonus(float ownInfluence, float enemyInfluence) {
		if (enemyInfluence >= 0.81f && ownInfluence <= 0.10f)
			return 1.2f;
		else if (enemyInfluence >= 0.81f || (enemyInfluence >= 0.71f && ownInfluence <= 0.10f))
			return 1.15f;
		else if (enemyInfluence >= 0.71f)
			return 1.1f;
		return 1f;
	}
}
