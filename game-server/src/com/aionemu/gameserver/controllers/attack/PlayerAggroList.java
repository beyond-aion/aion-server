package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public class PlayerAggroList extends AggroList {

	/**
	 * @param owner
	 */
	public PlayerAggroList(Creature owner) {
		super(owner);
	}

	@Override
	protected boolean isAware(Creature creature) {
		return creature != null && !creature.equals(owner);
	}
}
