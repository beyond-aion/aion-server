package com.aionemu.gameserver.geoEngine.scene.mesh;

import java.util.BitSet;

import com.aionemu.gameserver.geoEngine.collision.Collidable;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.scene.Geometry;
import com.aionemu.gameserver.geoEngine.scene.Mesh;

/**
 * @author MrPoke, Rolandas, Neon
 */
public class DoorGeometry extends Geometry {

	private BitSet instances = new BitSet();
	private boolean collideWithWorldBounds;

	public DoorGeometry(String name, Mesh mesh) {
		super(name, mesh);
		collideWithWorldBounds = isNotCompletelySolid(name);
	}

	/**
	 * @return True if the geometry is not a solid door, such as barricades or doors which only appear as one piece but consist of many pieces with
	 *         spaces in between. Such doors must be specially treated so skills will not accidentally go through.
	 */
	private boolean isNotCompletelySolid(String name) {
		return name.endsWith("ldf5_fortress_door_01.cgf") || name.contains("barricade");
	}

	public synchronized boolean isClosed(int instanceId) {
		return instances.get(instanceId);
	}

	public synchronized void setDoorState(int instanceId, boolean isOpened) {
		instances.set(instanceId, !isOpened);
	}

	@Override
	public int collideWith(Collidable other, CollisionResults results) {
		if (isClosed(results.getInstanceId())) {
			worldBound.setTreeCollidable(collideWithWorldBounds);
			return super.collideWith(other, results);
		}
		return 0;
	}

	@Override
	public DoorGeometry clone() throws CloneNotSupportedException {
		DoorGeometry clone = (DoorGeometry) super.clone();
		clone.instances = (BitSet) instances.clone();
		return clone;
	}
}
