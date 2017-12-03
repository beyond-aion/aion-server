package com.aionemu.gameserver.world.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;

/**
 * @author ATracer
 */
public class GeoService {

	private static final Logger log = LoggerFactory.getLogger(GeoService.class);
	private GeoData geoData;

	/**
	 * Initialize geodata based on configuration, load necessary structures
	 */
	public void initializeGeo() {
		ClassLoader.getSystemClassLoader().setPackageAssertionStatus("com.jme3", false); // disables unwanted assertion errors and optimizes runtime
		switch (getConfiguredGeoType()) {
			case GEO_MESHES:
				geoData = new RealGeoData();
				break;
			case NO_GEO:
				geoData = new DummyGeoData();
				break;
		}
		log.info("Configured Geo type: " + getConfiguredGeoType());
		geoData.loadGeoMaps();
	}

	public void setDoorState(int worldId, int instanceId, String name, boolean isOpened) {
		if (GeoDataConfig.GEO_ENABLE) {
			geoData.getMap(worldId).setDoorState(instanceId, name, isOpened);
		}
	}

	/**
	 * @return The surface Z coordinate at the objects position, nearest to the given zMax value at the given position or {@link Float#NaN} if not found
	 *         / less than zMin.
	 */
	public float getZ(VisibleObject object, float zMax, float zMin) {
		return getZ(object.getWorldId(), object.getX(), object.getY(), zMax, zMin, object.getInstanceId());
	}

	/**
	 * @return The highest found surface Z coordinate at the given position or {@link Float#NaN} if not found.
	 */
	public float getZ(int worldId, float x, float y) {
		return getZ(worldId, x, y, GeoMap.MAX_Z, GeoMap.MIN_Z, 1);
	}

	/**
	 * @return The surface Z coordinate nearest to the given Z value at the given position or {@link Float#NaN} if not found.
	 */
	public float getZ(int worldId, float x, float y, float z, int instanceId) {
		return getZ(worldId, x, y, z + 2, z - 2, instanceId);
	}

	/**
	 * @return The surface Z coordinate nearest to the given zMax value at the given position or {@link Float#NaN} if not found / less than zMin.
	 */
	public float getZ(int worldId, float x, float y, float zMax, float zMin, int instanceId) {
		return geoData.getMap(worldId).getZ(x, y, zMax, zMin, instanceId);
	}

	public String getDoorName(int worldId, String meshFile, float x, float y, float z) {
		return GeoDataConfig.GEO_DOORS_ENABLE ? geoData.getMap(worldId).getDoorName(worldId, meshFile, x, y, z) : null;
	}

	public CollisionResults getCollisions(VisibleObject object, float x, float y, float z, byte intentions) {
		return geoData.getMap(object.getWorldId()).getCollisions(object.getX(), object.getY(), object.getZ(), x, y, z, object.getInstanceId(),
			intentions);
	}

	/**
	 * @param object
	 * @param target
	 * @return
	 */
	public boolean canSee(VisibleObject object, VisibleObject target) {
		if (!GeoDataConfig.CANSEE_ENABLE)
			return true;

		// TODO: remove this check after fixing geo doors attacking
		if (target instanceof SiegeNpc && ((SiegeNpc) target).getAi().getName().equals("fortressgate"))
			return true;

		float objectSeeCheckZ = object.getZ() + getSeeCheckOffset(object);
		float targetSeeCheckZ = target.getZ() + getSeeCheckOffset(target);

		return canSee(object.getWorldId(), object.getX(), object.getY(), objectSeeCheckZ, target.getX(), target.getY(), targetSeeCheckZ,
			object.getInstanceId());
	}

	public boolean canSee(int worldId, float x, float y, float z, float x1, float y1, float z1, int instanceId) {
		return geoData.getMap(worldId).canSee(x, y, z, x1, y1, z1, instanceId);
	}

	private float getSeeCheckOffset(VisibleObject object) {
		float height = object.getObjectTemplate().getBoundRadius().getUpper();
		if (height > 2.5f)
			return height / 2;
		return 1.25f;
	}

	public Vector3f getClosestCollision(Creature object, float x, float y, float z) {
		return getClosestCollision(object, x, y, z, true, CollisionIntention.DEFAULT_COLLISIONS.getId());
	}

	public Vector3f getClosestCollision(Creature object, float x, float y, float z, boolean atNearGroundZ, byte intentions) {
		return geoData.getMap(object.getWorldId()).getClosestCollision(object.getX(), object.getY(), object.getZ(), x, y, z, atNearGroundZ,
			object.getInstanceId(), intentions);
	}

	public GeoType getConfiguredGeoType() {
		if (GeoDataConfig.GEO_ENABLE) {
			return GeoType.GEO_MESHES;
		}
		return GeoType.NO_GEO;
	}

	public static final GeoService getInstance() {
		return SingletonHolder.instance;
	}

	private static final class SingletonHolder {

		protected static final GeoService instance = new GeoService();
	}
}
