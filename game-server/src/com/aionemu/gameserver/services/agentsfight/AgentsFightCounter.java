package com.aionemu.gameserver.services.agentsfight;

import java.util.Map;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.google.common.collect.Maps;

/**
 * @author Yeats
 *
 */
public class AgentsFightCounter {

	private final Map<Race, AgentsFightRaceCounter> raceCounters = Maps.newHashMap();
	
	public AgentsFightCounter() {
		raceCounters.put(Race.ELYOS, new AgentsFightRaceCounter(Race.ELYOS));
		raceCounters.put(Race.ASMODIANS, new AgentsFightRaceCounter(Race.ASMODIANS));
	}
	
	public void addDamage(Player player, int damage) {
			raceCounters.get(player.getRace()).addPoints(player, damage);
	}
	
	public void addAP(Player player, int ap) {
		raceCounters.get(player.getRace()).addAP(player, ap);
	}
	
	public AgentsFightRaceCounter getRaceCounter(Race race) {
		return raceCounters.get(race);
	}
	
	public void clearAll() {
		raceCounters.get(Race.ELYOS).clear();
		raceCounters.get(Race.ASMODIANS).clear();
	}
}
