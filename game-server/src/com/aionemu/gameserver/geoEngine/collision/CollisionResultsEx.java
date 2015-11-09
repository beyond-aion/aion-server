package com.aionemu.gameserver.geoEngine.collision;

import com.jme3.collision.CollisionResults;

/**
 * Subclass of {@link com.jme3.collision.CollisionResults CollisionResults} which holds additional collision information.
 * 
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see CollisionResults
 */
public class CollisionResultsEx extends CollisionResults {

	private boolean onlyFirst;
	private byte intentions;
	private int instanceId;

	public CollisionResultsEx() {
		this((byte) 0, false, 0);
	}

	public CollisionResultsEx(byte intentions, boolean searchFirst, int instanceId) {
		super();
		this.intentions = intentions;
		this.onlyFirst = searchFirst;
		this.instanceId = instanceId;
	}

	/**
	 * Copies the attributes of given {@code results} to this object.
	 */
	public void copyAttributes(CollisionResultsEx results) {
		this.intentions = results.getIntentions();
		this.onlyFirst = results.isOnlyFirst();
		this.instanceId = results.getInstanceId();
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
