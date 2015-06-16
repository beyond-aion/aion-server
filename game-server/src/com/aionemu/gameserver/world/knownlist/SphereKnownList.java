package com.aionemu.gameserver.world.knownlist;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.utils.MathUtil;

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
	protected boolean checkReversedObjectInRange(VisibleObject newObject) {
		return MathUtil.isIn3dRange(owner, newObject, radius);
	}
}
