package com.aionemu.gameserver.world.zone;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Source
 */
public class InvasionZoneInstance extends ZoneInstance {

	private static final Logger log = LoggerFactory.getLogger(InvasionZoneInstance.class);
	private FastMap<Integer, Player> players = new FastMap<Integer, Player>();

	/**
	 * @param mapId
	 * @param template
	 * @param handler
	 */
	public InvasionZoneInstance(int mapId, ZoneInfo template) {
		super(mapId, template);
	}

	@Override
	public synchronized boolean onEnter(Creature creature) {
		if (super.onEnter(creature)) {
			if (creature instanceof Player) {
				players.put(creature.getObjectId(), (Player) creature);
			}
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean onLeave(Creature creature) {
		if (super.onLeave(creature)) {
			if (creature instanceof Player) {
				players.remove(creature.getObjectId());
			}
			return true;
		}
		return false;
	}

	public void doOnAllPlayers(Visitor<Player> visitor) {
		try {
			for (FastMap.Entry<Integer, Player> e : players.entrySet()) {
				Player player = e.getValue();
				if (player != null) {
					visitor.visit(player);
				}
			}
		}
		catch (Exception ex) {
			log.error("Exception when running visitor on all players" + ex);
		}
	}

}