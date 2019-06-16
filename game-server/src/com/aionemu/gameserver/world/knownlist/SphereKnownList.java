package com.aionemu.gameserver.world.knownlist;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;

/**
 * @author ATracer
 */
public class SphereKnownList extends PlayerAwareKnownList {

	private final float radius;

	public SphereKnownList(VisibleObject owner, float radius) {
		super(owner);
		this.radius = radius;
	}

	@Override
	protected float getVisibleDistance(VisibleObject newObject) {
		return radius;
	}
}
