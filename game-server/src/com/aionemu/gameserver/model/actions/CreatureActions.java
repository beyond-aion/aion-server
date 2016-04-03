package com.aionemu.gameserver.model.actions;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author xTz
 */
public class CreatureActions extends VisibleObjectActions {

	public static boolean isAlreadyDead(Creature creature) {
		return creature.getLifeStats().isAlreadyDead();
	}
}
