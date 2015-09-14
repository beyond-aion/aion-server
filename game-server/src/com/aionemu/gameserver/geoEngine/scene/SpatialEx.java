package com.aionemu.gameserver.geoEngine.scene;

import com.aionemu.gameserver.geoEngine.collision.CollisionResultsEx;
import com.jme3.collision.Collidable;
import com.jme3.scene.Spatial;

/**
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see Spatial
 */
public interface SpatialEx {

	default public byte getMaterialId() {
		return (byte) (getCollisionFlags() & 0xFF);
	}

	default public byte getIntentions() {
		return (byte) (getCollisionFlags() >> 8);
	}

	public short getCollisionFlags();

	public void setCollisionFlags(short flags);

	public int collideWith(Collidable other, CollisionResultsEx results);

}
