package com.aionemu.gameserver.world.geo;

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
	public final float getZ(float x, float y) {
		return Float.NaN;
	}

	@Override
	public final float getZ(float x, float y, float zMax, float zMin, int instanceId) {
		return Float.NaN;
	}

	@Override
	public final boolean canSee(float x, float y, float z, float targetX, float targetY, float targetZ, float limit, int instanceId) {
		return true;
	}

	@Override
	public Vector3f getClosestCollision(float x, float y, float z, float targetX, float targetY, float targetZ, boolean atNearGroundZ, int instanceId,
		byte intentions) {
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
