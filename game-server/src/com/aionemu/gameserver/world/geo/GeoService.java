package com.aionemu.gameserver.world.geo;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.GeoWorldLoader;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseDoorState;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class GeoService implements GameEngine {

	private final Map<Integer, GeoMap> geoMaps = new HashMap<>();

	@Override
	public void init() {
		DataManager.WORLD_MAPS_DATA.forEach(map -> geoMaps.put(map.getMapId(), new GeoMap(map.getMapId())));
		if (GeoDataConfig.GEO_ENABLE) {
			GeoWorldLoader.load(geoMaps.values());
		} else {
			LoggerFactory.getLogger(GeoService.class).warn("Geo data is disabled");
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
	 * @return The surface Z coordinate nearest to the given Z value at the given position or {@link Float#NaN} if not found.
	 */
	public float getZ(int worldId, float x, float y, float z, int instanceId) {
		return getZ(worldId, x, y, z + 2, z - 2, instanceId);
	}

	/**
	 * @return The surface Z coordinate nearest to the given zMax value at the given position or {@link Float#NaN} if not found / less than zMin.
	 */
	public float getZ(int worldId, float x, float y, float zMax, float zMin, int instanceId) {
		return geoMaps.get(worldId).getZ(x, y, zMax, zMin, instanceId);
	}

	public CollisionResults getCollisions(VisibleObject object, float x, float y, float z, byte intentions, IgnoreProperties ignoreProperties) {
		return geoMaps.get(object.getWorldId()).getCollisions(object.getX(), object.getY(), object.getZ() + getSeeCheckOffset(object), x, y, z,
			object.getInstanceId(), intentions, ignoreProperties);
	}

	/**
	 * @return True if object has unobstructed view on its target.
	 */
	public boolean canSee(VisibleObject object, VisibleObject target) {
		if (!GeoDataConfig.CANSEE_ENABLE)
			return true;

		float objectSeeCheckZ = object.getZ() + getSeeCheckOffset(object);
		float targetSeeCheckZ = target.getZ() + getSeeCheckOffset(target);
		float x = object.getX();
		float y = object.getY();
		float targetX = target.getX();
		float targetY = target.getY();
		if (object instanceof Npc npc && npc.getAi().ask(AIQuestion.CONSIDER_BOUNDS_IN_CAN_SEE_CHECK_WHEN_ATTACKING)) {
			double rad = Math.toRadians(PositionUtil.calculateAngleFrom(object, target));
			x += (float) (Math.cos(rad) * object.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide());
			y += (float) (Math.sin(rad) * object.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide());
		}
		if (target instanceof Npc npc && npc.getAi().ask(AIQuestion.CONSIDER_BOUNDS_IN_CAN_SEE_CHECK_WHEN_ATTACKED)) {
			double rad = Math.toRadians(PositionUtil.calculateAngleFrom(target, object));
			targetX += (float) (Math.cos(rad) * target.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide());
			targetY += (float) (Math.sin(rad) * target.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide());
		}
		Race race = null;
		int staticId = -1;
		if (target.getSpawn() != null) {
			staticId = target.getSpawn().getStaticId();
		}
		if (object instanceof Creature creature) {
			race = creature.getRace();
		}
		IgnoreProperties ignoreProperties = IgnoreProperties.of(race, staticId);
		return geoMaps.get(object.getWorldId()).canSee(x, y, objectSeeCheckZ, targetX, targetY, targetSeeCheckZ, object.getInstanceId(), ignoreProperties);
	}

	public boolean canSee(VisibleObject object, float targetX, float targetY, float targetZ, IgnoreProperties ignoreProperties) {
		float zOffset = getSeeCheckOffset(object);
		return geoMaps.get(object.getWorldId()).canSee(object.getX(), object.getY(), object.getZ() + zOffset, targetX, targetY, targetZ + zOffset,
			object.getInstanceId(), ignoreProperties);
	}

	private float getSeeCheckOffset(VisibleObject object) {
		float height = object.getObjectTemplate().getBoundRadius().getUpper();
		if (object instanceof Player p && p.isTransformed() && p.getTransformModel().getBanMovement() == 1) {
			NpcTemplate t = DataManager.NPC_DATA.getNpcTemplate(p.getTransformModel().getModelId());
			if (t != null)
				return t.getBoundRadius().getUpper();
		}
		return height > 2.5f ? height / 2 : 1.25f;
	}

	public Vector3f getClosestCollision(Creature object, float x, float y, float z) {
		return getClosestCollision(object, x, y, z, true, CollisionIntention.DEFAULT_COLLISIONS.getId(), IgnoreProperties.ANY_RACE);
	}

	public Vector3f getClosestCollision(Creature object, float x, float y, float z, boolean atNearGroundZ, byte intentions, IgnoreProperties ignoreProperties) {
		return geoMaps.get(object.getWorldId()).getClosestCollision(object.getX(), object.getY(), object.getZ(), x, y, z, atNearGroundZ,
			object.getInstanceId(), intentions, ignoreProperties);
	}

	/**
	 * Terrain agnostic check. It will move along the terrain and only return collisions which are actual obstacles, like trees, walls or steep hills.
	 * Inclines <= 45° will not be considered as collisions.
	 * On steep slopes and cliffs it'll return the position nearest to where you can still walk.
	 */
	public Vector3f findMovementCollision(Creature creature, float directionAngle, float maxDistance) {
		double rad = Math.toRadians(directionAngle);
		float x1 = (float) (Math.cos(rad) * maxDistance);
		float y1 = (float) (Math.sin(rad) * maxDistance);
		Vector3f startPos;
		GeoMap map = geoMaps.get(creature.getWorldId());
		if (creature instanceof Player player) {
			startPos = calculateCurrentGeoPosition(player);
			if (creature.isFlying())
				return map.getClosestCollision(startPos.getX(), startPos.getY(), startPos.getZ(), startPos.getX() + x1, startPos.getY() + y1,
					startPos.getZ(), false, creature.getInstanceId(), CollisionIntention.DEFAULT_COLLISIONS.getId(), IgnoreProperties.ANY_RACE);
		} else
			startPos = new Vector3f(creature.getX(), creature.getY(), creature.getZ());
		return map.findMovementCollision(startPos, startPos.getX() + x1, startPos.getY() + y1, creature.getInstanceId());
	}

	private Vector3f calculateCurrentGeoPosition(Player player) {
		WorldPosition approximatePos = player.getPosition();
		WorldPosition lastPos = player.getMoveController().getLastPositionFromClient();
		if (lastPos == null)
			return new Vector3f(approximatePos.getX(), approximatePos.getY(), approximatePos.getZ());
		// client sends CM_MOVE in intervals when moving straight, so we search for possible collisions between lastPos and the server side position
		return geoMaps.get(approximatePos.getMapId()).getClosestCollision(lastPos.getX(), lastPos.getY(), lastPos.getZ(), approximatePos.getX(),
			approximatePos.getY(), approximatePos.getZ(), true, approximatePos.getInstanceId(), CollisionIntention.DEFAULT_COLLISIONS.getId(),
			IgnoreProperties.ANY_RACE);
	}

	public void spawnPlaceableObject(int worldId, int instanceId, int staticId) {
		geoMaps.get(worldId).spawnPlaceableObject(instanceId, staticId);
	}

	public void despawnPlaceableObject(int worldId, int instanceId, int staticId) {
		geoMaps.get(worldId).despawnPlaceableObject(instanceId, staticId);
	}

	public void updateTown(Race race, int townId, int level) {
		switch (race) {
			case ELYOS -> geoMaps.get(WorldMapType.ORIEL.getId()).updateTownToLevel(townId, level);
			case ASMODIANS -> geoMaps.get(WorldMapType.PERNON.getId()).updateTownToLevel(townId, level);
		}
	}

	public void setHouseDoorState(int worldId, int instanceId, int houseAddress, HouseDoorState state) {
		geoMaps.get(worldId).setHouseDoorState(instanceId, houseAddress, state);
	}

	public void setDoorState(int worldId, int instanceId, int doorId, boolean open) {
		geoMaps.get(worldId).setDoorState(instanceId, doorId, open);
	}

	public boolean worldHasTerrainMaterials(int worldId) {
		return GeoDataConfig.GEO_MATERIALS_ENABLE && geoMaps.get(worldId).hasTerrainMaterials();
	}

	public int getTerrainMaterialAt(int worldId, float x, float y, float z, int instanceId) {
		return GeoDataConfig.GEO_MATERIALS_ENABLE ? geoMaps.get(worldId).getTerrainMaterialAt(x, y, z, instanceId) : 0;
	}

	public static GeoService getInstance() {
		return SingletonHolder.instance;
	}

	private static final class SingletonHolder {

		protected static final GeoService instance = new GeoService();
	}
}
