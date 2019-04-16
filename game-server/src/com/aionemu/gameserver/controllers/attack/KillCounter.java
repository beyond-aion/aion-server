package com.aionemu.gameserver.controllers.attack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.configs.main.CustomConfig;

/**
 * @author Neon
 */
public class KillCounter {

	private static final Map<Integer, Map<Integer, List<Long>>> PVP_KILL_LISTS = new ConcurrentHashMap<>();

	/**
	 * Increments the killers kill counter for the victim by one and returns the updated count. Old kills get removed over time, so the returned value
	 * represents only kills in the past 24 hours (configurable, see {@link CustomConfig#PVP_DAY_DURATION}).
	 * 
	 * @return The count how many times the killer killed given victim.
	 */
	public static int addKillFor(int killerId, int victimId) {
		long now = System.currentTimeMillis();
		long minAge = now - CustomConfig.PVP_DAY_DURATION;
		Map<Integer, List<Long>> killTimesByVictimId = PVP_KILL_LISTS.computeIfAbsent(killerId, k -> new HashMap<>());
		synchronized (killTimesByVictimId) {
			List<Long> killTimes = killTimesByVictimId.computeIfAbsent(victimId, k -> new ArrayList<>());
			killTimes.removeIf(time -> time < minAge);
			killTimes.add(now);
			return killTimes.size();
		}
	}
}
