package com.aionemu.gameserver.world.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionResultsEx;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.utils.MathUtil;
import com.jme3.math.Vector3f;

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
	 * @param object
	 * @return
	 */
	public float getZ(VisibleObject object) {
		return geoData.getMap(object.getWorldId()).getZ(object.getX(), object.getY(), object.getZ(), object.getInstanceId());
	}

	/**
	 * @param worldId
	 * @param x
	 * @param y
	 * @param z
	 * @param defaultUp
	 * @return
	 */
	public float getZ(int worldId, float x, float y, float z, float defaultUp, int instanceId) {
		float newZ = geoData.getMap(worldId).getZ(x, y, z, instanceId);
		return GeoDataConfig.GEO_ENABLE ? newZ : newZ + defaultUp;
	}

	/**
	 * @param worldId
	 * @param x
	 * @param y
	 * @return
	 */
	public float getZ(int worldId, float x, float y) {
		return geoData.getMap(worldId).getZ(x, y);
	}

	public String getDoorName(int worldId, String meshFile, float x, float y, float z) {
		return geoData.getMap(worldId).getDoorName(worldId, meshFile, x, y, z);
	}

	public CollisionResultsEx getCollisions(VisibleObject object, float x, float y, float z, boolean changeDirection, byte intentions) {
		return geoData.getMap(object.getWorldId()).getCollisions(object.getX(), object.getY(), object.getZ(), x, y, z, changeDirection, false,
			object.getInstanceId(), intentions);
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
		if (target instanceof SiegeNpc && ((SiegeNpc) target).getObjectTemplate().getAi().equals("fortressgate"))
			return true;

		float limit = (float) (MathUtil.getDistance(object, target) - target.getObjectTemplate().getBoundRadius().getCollision());
		if (limit <= 0)
			return true;

		return geoData.getMap(object.getWorldId()).canSee(object.getX(), object.getY(),
			object.getZ() + object.getObjectTemplate().getBoundRadius().getUpper() * 0.95f, target.getX(), target.getY(),
			target.getZ() + target.getObjectTemplate().getBoundRadius().getUpper() * 0.75f, limit, object.getInstanceId());
	}

	public boolean canSee(int worldId, float x, float y, float z, float x1, float y1, float z1, float limit, int instanceId) {
		return geoData.getMap(worldId).canSee(x, y, z + 1, x1, y1, z1 + 1, limit, instanceId);
	}

	public boolean isGeoOn() {
		return GeoDataConfig.GEO_ENABLE;
	}

	public Vector3f getClosestCollision(Creature object, float x, float y, float z, boolean changeDirection, byte intentions) {
		return geoData.getMap(object.getWorldId()).getClosestCollision(object.getX(), object.getY(), object.getZ(), x, y, z, changeDirection,
			object.isInFlyingState(), object.getInstanceId(), intentions);
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

	@SuppressWarnings("synthetic-access")
	private static final class SingletonHolder {

		protected static final GeoService instance = new GeoService();
	}
}
