package com.aionemu.gameserver.model.actions;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 *
 * @author xTz
 */
public class CreatureActions {

	public static String getName(Creature creature) {
		return creature.getName();
	}

	public static boolean isAlreadyDead(Creature creature) {
		return creature.getLifeStats().isAlreadyDead();
	}

	public static void delete(Creature creature) {
		if (creature != null) {
			creature.getController().onDelete();
		}
	}
}
