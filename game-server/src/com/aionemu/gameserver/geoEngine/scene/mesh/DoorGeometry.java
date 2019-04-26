package com.aionemu.gameserver.geoEngine.scene.mesh;

import java.util.BitSet;

import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.collision.Collidable;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.scene.Geometry;

/**
 * @author MrPoke, Rolandas
 */
public class DoorGeometry extends Geometry {

	private BitSet instances = new BitSet();
	private boolean foundTemplate = false;

	public DoorGeometry(String name) {
		super(name);
	}

	public void setDoorState(int instanceId, boolean isOpened) {
		instances.set(instanceId, isOpened);
	}

	@Override
	public int collideWith(Collidable other, CollisionResults results) {
		if (foundTemplate && instances.get(results.getInstanceId()))
			return 0;
		if (other instanceof Ray) // no collision if inside arena spheres, so just check volume
			return getWorldBound().collideWith(other, results);
		return super.collideWith(other, results);
	}

	public boolean isFoundTemplate() {
		return foundTemplate;
	}

	public void setFoundTemplate(boolean foundTemplate) {
		this.foundTemplate = foundTemplate;
	}

	@Override
	public void updateModelBound() {
		if (worldBound == null) {
			mesh.updateBound();
			worldBound = new BoundingBox((BoundingBox)mesh.getBound());
		}
	}

	@Override
	public DoorGeometry clone() throws CloneNotSupportedException {
		DoorGeometry clone = (DoorGeometry) super.clone();
		clone.instances = (BitSet) instances.clone();
		return clone;
	}
}
