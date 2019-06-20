package com.aionemu.gameserver.geoEngine.scene.mesh;

import java.util.BitSet;

import com.aionemu.gameserver.geoEngine.collision.Collidable;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.scene.Geometry;
import com.aionemu.gameserver.geoEngine.scene.Mesh;

/**
 * @author MrPoke, Rolandas
 */
public class DoorGeometry extends Geometry {

	private BitSet instances = new BitSet();

	public DoorGeometry(String name, Mesh mesh) {
		super(name, mesh);
	}

	public synchronized boolean isClosed(int instanceId) {
		return instances.get(instanceId);
	}

	public synchronized void setDoorState(int instanceId, boolean isOpened) {
		instances.set(instanceId, !isOpened);
	}

	@Override
	public int collideWith(Collidable other, CollisionResults results) {
		if (isClosed(results.getInstanceId()))
			return super.collideWith(other, results);
		return 0;
	}

	@Override
	public DoorGeometry clone() throws CloneNotSupportedException {
		DoorGeometry clone = (DoorGeometry) super.clone();
		clone.instances = (BitSet) instances.clone();
		return clone;
	}
}
