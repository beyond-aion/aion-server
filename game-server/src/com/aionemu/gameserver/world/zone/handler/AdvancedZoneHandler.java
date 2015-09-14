package com.aionemu.gameserver.world.zone.handler;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author MrPoke
 */
public interface AdvancedZoneHandler extends ZoneHandler {

	/**
	 * This call if creature die in zone.
	 * 
	 * @param attacker
	 * @param target
	 * @return TRUE if hadle die event.
	 */
	public boolean onDie(Creature attacker, Creature target, ZoneInstance zone);

}
