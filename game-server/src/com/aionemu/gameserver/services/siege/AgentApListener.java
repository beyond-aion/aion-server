package com.aionemu.gameserver.services.siege;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Yeats
 */
public class AgentApListener {

	private final Race race;
	private final AtomicLong totalDmg = new AtomicLong();
	private final AtomicLong totalAP = new AtomicLong();
	private final Map<Integer, AtomicLong> playerAP = new ConcurrentHashMap<>();
	private final Map<Integer, AtomicLong> playerDamage = new ConcurrentHashMap<>();

	public AgentApListener(Race race) {
		this.race = race;
	}

	public long getTotalDmg() {
		return totalDmg.get();
	}

	public long getTotalAp() {
		return totalAP.get();
	}

	public Map<Integer, Long> getPlayerAbyssPoints() {
		return getOrderedCounterMap(playerAP);
	}

	public Map<Integer, Long> getPlayerDamage() {
		return getOrderedCounterMap(playerDamage);
	}

	/**
	 * @param player
	 * @param damage
	 */
	public void addPoints(Player player, int damage) {
		totalDmg.addAndGet(damage);
		addToCounter(player.getObjectId(), damage, playerDamage);
	}

	/**
	 * @param player
	 * @param ap
	 */
	public void addAP(Player player, int ap) {
		totalAP.addAndGet(ap);
		addToCounter(player.getObjectId(), ap, playerAP);
	}

	private <K> void addToCounter(K key, int value, Map<K, AtomicLong> counterMap) {
		AtomicLong counter = counterMap.get(key);

		if (counter == null) {
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

	private <K> Map<K, Long> getOrderedCounterMap(Map<K, AtomicLong> unorderedMap) {
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

	public Race getRace() {
		return race;
	}

	public void clear() {
		totalDmg.set(0);
		totalAP.set(0);
		playerAP.clear();
		playerDamage.clear();
	}
}
