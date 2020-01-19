package com.aionemu.gameserver.world.geo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.house.HouseDoorState;

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
	public CollisionResults getCollisions(VisibleObject object, float x, float y, float z, byte intentions, IgnoreProperties ignoreProperties) {
		return geoData.getMap(object.getWorldId()).getCollisions(object.getX(), object.getY(), object.getZ(), x, y, z, object.getInstanceId(),
			intentions, ignoreProperties);
	}

	/**
	 * @return True if object has unobstructed view on its target.
	 */
	public boolean canSee(VisibleObject object, VisibleObject target) {
		if (!GeoDataConfig.CANSEE_ENABLE)
			return true;

		float objectSeeCheckZ = object.getZ() + getSeeCheckOffset(object);
		float targetSeeCheckZ = target.getZ() + getSeeCheckOffset(target);
		Race race = null;
		int staticId = -1;
		if (target.getSpawn() != null) {
			staticId = target.getSpawn().getStaticId();
		}
		if (object instanceof Creature) {
			race = ((Creature) object).getRace();
		}
		IgnoreProperties ignoreProperties = IgnoreProperties.of(race, staticId);
		return geoData.getMap(object.getWorldId()).canSee(object.getX(), object.getY(), objectSeeCheckZ, target.getX(),
			target.getY(), targetSeeCheckZ, object.getInstanceId(), ignoreProperties);
	}

	public boolean canSee(int worldId, int instanceId, float x, float y, float z, float targetX, float targetY, float targetZ, float zOffset, IgnoreProperties ignoreProperties) {
		return geoData.getMap(worldId).canSee(x, y, z + zOffset, targetX, targetY, targetZ + zOffset, instanceId, ignoreProperties);
	}

	private float getSeeCheckOffset(VisibleObject object) {
		float height = object.getObjectTemplate().getBoundRadius().getUpper();
		if (height > 2.5f)
			return height / 2;
		return 1.25f;
	}

	public Vector3f getClosestCollision(Vector3f startPosition, int worldId, int instanceId, float x, float y, float z,  IgnoreProperties ignoreProperties) {
		return geoData.getMap(worldId).getClosestCollision(startPosition.getX(), startPosition.getY(), startPosition.getZ(), x, y, z, true,
				instanceId, CollisionIntention.DEFAULT_COLLISIONS.getId(), ignoreProperties);
	}

	public Vector3f getClosestCollision(Creature object, float x, float y, float z, IgnoreProperties ignoreProperties) {
		return getClosestCollision(object, x, y, z, true, CollisionIntention.DEFAULT_COLLISIONS.getId(), ignoreProperties);
	}

	public Vector3f getClosestCollision(Creature object, float x, float y, float z, boolean atNearGroundZ, byte intentions, IgnoreProperties ignoreProperties) {
		return geoData.getMap(object.getWorldId()).getClosestCollision(object.getX(), object.getY(), object.getZ(), x, y, z, atNearGroundZ,
			object.getInstanceId(), intentions, ignoreProperties);
	}

	public void spawnPlaceableObject(int worldId, int instanceId, int staticId) {
		if (GeoDataConfig.GEO_ENABLE) {
			geoData.getMap(worldId).spawnPlaceableObject(instanceId, staticId);
		}
	}

	public void despawnPlaceableObject(int worldId, int instanceId, int staticId) {
		if (GeoDataConfig.GEO_ENABLE) {
			geoData.getMap(worldId).despawnPlaceableObject(instanceId, staticId);
		}
	}

	public void updateTown(Race race, int townId, int level) {
		if (GeoDataConfig.GEO_ENABLE) {
			switch (race) {
				case ELYOS:
					geoData.getMap(700010000).updateTownToLevel(townId, level);
					break;
				case ASMODIANS:
					geoData.getMap(710010000).updateTownToLevel(townId, level);
					break;
			}
		}
	}

	public void setHouseDoorState(int worldId, int instanceId, int houseAddress, HouseDoorState state) {
		if (GeoDataConfig.GEO_ENABLE) {
			geoData.getMap(worldId).setHouseDoorState(instanceId, houseAddress, state);
		}
	}

	public void setDoorState(int worldId, int instanceId, int doorId, boolean open) {
		if (GeoDataConfig.GEO_ENABLE && GeoDataConfig.GEO_DOORS_ENABLE) {
			geoData.getMap(worldId).setDoorState(instanceId, doorId, open);
		}
	}

	public boolean worldHasTerrainMaterials(int worldId) {
		if (GeoDataConfig.GEO_ENABLE && GeoDataConfig.GEO_MATERIALS_ENABLE) {
			return geoData.getMap(worldId).hasTerrainMaterials();
		}
		return false;
	}

	public int getTerrainMaterialAt(int worldId, float x, float y, float z, int instanceId) {
		if (GeoDataConfig.GEO_ENABLE && GeoDataConfig.GEO_MATERIALS_ENABLE) {
			return geoData.getMap(worldId).getTerrainMaterialAt(x, y, z, instanceId);
		}
		return 0;
	}

	public GeoType getConfiguredGeoType() {
		if (GeoDataConfig.GEO_ENABLE) {
			return GeoType.GEO_MESHES;
		}
		return GeoType.NO_GEO;
	}

	public List<Node> getGeometries(int worldId, String name) {
		if (GeoDataConfig.GEO_ENABLE) {
			return geoData.getMap(worldId).getGeometries(name);
		}
		return null;
	}

	public static GeoService getInstance() {
		return SingletonHolder.instance;
	}

	private static final class SingletonHolder {

		protected static final GeoService instance = new GeoService();
	}
}
