package com.aionemu.gameserver.world.geo;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Spatial;

/**
 * @author ATracer
 */
public class DummyGeoMap extends GeoMap {

	public DummyGeoMap(String name, int worldSize) {
		super(name, worldSize);
	}

	@Override
	public final float getZ(float x, float y, float zMax, float zMin, int instanceId) {
		return Float.NaN;
	}

	@Override
	public final boolean canSee(float x, float y, float z, float targetX, float targetY, float targetZ, int instanceId) {
		return true;
	}

	@Override
	public Vector3f getClosestCollision(float x, float y, float z, float targetX, float targetY, float targetZ, boolean atNearGroundZ, int instanceId,
		byte intentions) {
		if ((intentions & CollisionIntention.WALK.getId()) == 0) // if it's no walking npc, add 0.5m to not fall through the map on hills
			targetZ += 0.5f;
		return new Vector3f(targetX, targetY, targetZ);
	}

	@Override
	public String getDoorName(int worldId, String meshFile, float x, float y, float z) {
		return null;
	}

	@Override
	public void setDoorState(int instanceId, String name, boolean state) {

	}

	@Override
	public int attachChild(Spatial child) {
		return 0;
	}
}
