package com.aionemu.gameserver.services.siegeservice;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.world.World;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that contains all the counters for the siege. One SiegeCounter per race should be used.
 *
 * @author SoulKeeper
 */
public class SiegeRaceCounter implements Comparable<SiegeRaceCounter> {

	private final AtomicLong totalDamage = new AtomicLong();

	private final Map<Integer, AtomicLong> playerDamageCounter = new ConcurrentHashMap<>();

	private final Map<Integer, AtomicLong> playerAPCounter = new ConcurrentHashMap<>();

	private final SiegeRace siegeRace;

	public SiegeRaceCounter(SiegeRace siegeRace) {
		this.siegeRace = siegeRace;
	}

	public void addPoints(Creature creature, int damage) {

		addTotalDamage(damage);

		if (creature instanceof Player) {
			addPlayerDamage((Player) creature, damage);
		}
	}

	public void addTotalDamage(int damage) {
		totalDamage.addAndGet(damage);
	}

	public void addPlayerDamage(Player player, int damage) {
		addToCounter(player.getObjectId(), damage, playerDamageCounter);
	}

	public void addAbyssPoints(Player player, int abyssPoints) {
		addToCounter(player.getObjectId(), abyssPoints, playerAPCounter);
	}

	protected <K> void addToCounter(K key, int value, Map<K, AtomicLong> counterMap) {

		// Get the counter for specific key
		AtomicLong counter = counterMap.get(key);

		// Counter was not registered, need to create it
		if (counter == null) {

			// synchronize here, it may happen that there will be attempt to increment
			// same counter from different threads
			synchronized (this) {
				if (counterMap.containsKey(key)) {
					counter = counterMap.get(key);
				} else {
					counter = new AtomicLong();
					counterMap.put(key, counter);
				}
			}
		}

		counter.addAndGet(value);
	}

	public long getTotalDamage() {
		return totalDamage.get();
	}

	/**
	 * Returns "playerId to damage" map.
	 * Map is ordered by damage in "descending" order
	 *
	 * @return map with player damages
	 */
	public Map<Integer, Long> getPlayerDamageCounter() {
		return getOrderedCounterMap(playerDamageCounter);
	}

	/**
	 * Returns "player to abyss points" map.
	 * Map is ordered by abyssPoints in descending order
	 *
	 * @return map with player abyss points
	 */
	public Map<Integer, Long> getPlayerAbyssPoints() {
		return getOrderedCounterMap(playerAPCounter);
	}

	protected <K> Map<K, Long> getOrderedCounterMap(Map<K, AtomicLong> unorderedMap) {
		if (GenericValidator.isBlankOrNull(unorderedMap)) {
			return Collections.emptyMap();
		}

		LinkedList<Map.Entry<K, AtomicLong>> tempList = Lists.newLinkedList(unorderedMap.entrySet());
		Collections.sort(tempList, new Comparator<Map.Entry<K, AtomicLong>>() {
			@Override
			public int compare(Map.Entry<K, AtomicLong> o1, Map.Entry<K, AtomicLong> o2) {
				return new Long(o2.getValue().get()).compareTo(o1.getValue().get());
			}
		});

		Map<K, Long> result = Maps.newLinkedHashMap();
		for (Map.Entry<K, AtomicLong> entry : tempList) {
			if (entry.getValue().get() > 0) {
				result.put(entry.getKey(), entry.getValue().get());
			}
		}
		return result;
	}

	@Override
	public int compareTo(SiegeRaceCounter o) {
		return new Long(o.getTotalDamage()).compareTo(getTotalDamage());
	}

	public SiegeRace getSiegeRace() {
		return siegeRace;
	}

	/**
	 * Returns Legion of the Leader of the strongest Team
	 *
	 * @return legion id or null if none
	 */
	public Integer getWinnerLegionId() {
		Map<Player, AtomicLong> teamDamageMap = new HashMap<Player, AtomicLong>();
		for (Integer id : playerDamageCounter.keySet()) {
			Player player = World.getInstance().findPlayer(id);
			
			if (player != null) {
				if (player.getCurrentTeam() != null ) {
					if (!player.isInLeague()) {
						Player teamLeader = player.getCurrentTeam().getLeaderObject();
						long damage = playerDamageCounter.get(id).get();
						if (teamLeader != null) {
							if (!teamDamageMap.containsKey(teamLeader)) {
								teamDamageMap.put(teamLeader, new AtomicLong());
							}
							teamDamageMap.get(teamLeader).addAndGet(damage);
						}
					}
					else {
						Player teamLeader = player.getPlayerAlliance2().getLeague().getLeaderObject().getLeaderObject();
						long damage = playerDamageCounter.get(id).get();
						if (teamLeader != null) {
							if (!teamDamageMap.containsKey(teamLeader)) {
								teamDamageMap.put(teamLeader, new AtomicLong());
							}
							teamDamageMap.get(teamLeader).addAndGet(damage);
				   }
				}
			 }
			 else { //solo
				long damage = playerDamageCounter.get(id).get();
				if (!teamDamageMap.containsKey(player)) {
				   teamDamageMap.put(player, new AtomicLong());
				}
				teamDamageMap.get(player).addAndGet(damage);
			 }
		  }
	   }
	   if (teamDamageMap.isEmpty()) {
			return null;
		}
		
		Player topTeamLeader = getOrderedCounterMap(teamDamageMap).keySet().iterator().next();
		Legion legion = topTeamLeader.getLegion();

		return legion != null ? legion.getLegionId() : null;
	}
}
