package com.aionemu.gameserver.world.knownlist;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

/**
 * @author ATracer
 */
public class CreatureAwareKnownList extends KnownList {

	public CreatureAwareKnownList(VisibleObject owner) {
		super(owner);
	}

	@Override
	protected final boolean isAwareOf(VisibleObject newObject) {
		return newObject instanceof Creature;
	}
}
