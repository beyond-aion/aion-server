package com.aionemu.gameserver.world.knownlist;

import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

/**
 * @author ATracer
 */
public class NpcKnownList extends CreatureAwareKnownList {

	public NpcKnownList(VisibleObject owner) {
		super(owner);
	}

	@Override
	public void doUpdate() {
		if (owner.getPosition().isMapRegionActive())
			super.doUpdate();
		else
			clear(ObjectDeleteAnimation.FADE_OUT);
	}
}
