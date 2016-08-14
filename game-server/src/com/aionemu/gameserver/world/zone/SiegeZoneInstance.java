package com.aionemu.gameserver.world.zone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author MrPoke
 */
public class SiegeZoneInstance extends ZoneInstance {

	private static final Logger log = LoggerFactory.getLogger(SiegeZoneInstance.class);
	private Map<Integer, Player> players = new ConcurrentHashMap<>();

	/**
	 * @param mapId
	 * @param template
	 * @param handler
	 */
	public SiegeZoneInstance(int mapId, ZoneInfo template) {
		super(mapId, template);
	}

	@Override
	public synchronized boolean onEnter(Creature creature) {
		if (super.onEnter(creature)) {
			if (creature instanceof Player)
				players.put(creature.getObjectId(), (Player) creature);
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean onLeave(Creature creature) {
		if (super.onLeave(creature)) {
			if (creature instanceof Player)
				players.remove(creature.getObjectId());
			return true;
		}
		return false;
	}

	public void forEachPlayer(Visitor<Player> visitor) {
		try {
			players.values().forEach(player -> {
				if (player != null) // can be null if entry got removed after iterator allocation
					visitor.visit(player);
			});
		} catch (Exception ex) {
			log.error("Exception when running visitor on all players", ex);
		}
	}
}
