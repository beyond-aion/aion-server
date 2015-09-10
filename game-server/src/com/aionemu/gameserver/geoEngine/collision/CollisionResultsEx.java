package com.aionemu.gameserver.geoEngine.collision;

import com.jme3.collision.CollisionResults;

/**
 * Subclass of {@link com.jme3.collision.CollisionResults CollisionResults} which holds additional collision information.
 * 
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see CollisionResults
 */
public class CollisionResultsEx extends CollisionResults {

	private final boolean onlyFirst;
	private final byte intentions;
	private final int instanceId;

	public CollisionResultsEx(byte intentions, boolean searchFirst, int instanceId) {
		super();
		this.intentions = intentions;
		this.onlyFirst = searchFirst;
		this.instanceId = instanceId;
	}

	public boolean isOnlyFirst() {
		return onlyFirst;
	}

	public byte getIntentions() {
		return intentions;
	}

	public int getInstanceId() {
		return instanceId;
	}

}
