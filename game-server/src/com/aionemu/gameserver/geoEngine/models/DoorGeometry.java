package com.aionemu.gameserver.geoEngine.models;

import java.util.BitSet;

import com.aionemu.gameserver.geoEngine.collision.CollisionResultsEx;
import com.aionemu.gameserver.geoEngine.scene.GeometryEx;
import com.aionemu.gameserver.geoEngine.scene.MeshEx;
import com.jme3.collision.Collidable;
import com.jme3.math.Ray;

/**
 * This class holds a doors geometric mesh (currently box for simplicity) and state information (open/closed) for collision detection.
 * 
 * @author MrPoke, Rolandas
 * @modified Neon
 */
public class DoorGeometry extends GeometryEx {

	BitSet instances = new BitSet();
	private boolean foundTemplate = false;

	public DoorGeometry(String name, MeshEx mesh) {
		super(name, mesh);
	}

	public boolean isFoundTemplate() {
		return foundTemplate;
	}

	public void setFoundTemplate(boolean foundTemplate) {
		this.foundTemplate = foundTemplate;
	}

	public void setDoorState(int instanceId, boolean isOpened) {
		instances.set(instanceId, isOpened);
	}

	@Override
	public int collideWith(Collidable other, CollisionResultsEx results) {
		if (foundTemplate && instances.get(results.getInstanceId()))
			return 0;

		// no collision if inside arena spheres, so just check volume
		if (other instanceof Ray)
			return getWorldBound().collideWith(other, results);

		return super.collideWith(other, results);
	}
}
