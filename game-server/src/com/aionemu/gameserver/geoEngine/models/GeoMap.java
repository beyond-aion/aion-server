package com.aionemu.gameserver.geoEngine.models;

import java.util.*;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector2f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.DespawnableNode;
import com.aionemu.gameserver.geoEngine.scene.DespawnableNode.DespawnableType;
import com.aionemu.gameserver.geoEngine.scene.Geometry;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.house.HouseDoorState;
import com.aionemu.gameserver.world.RegionUtil;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Mr. Poke
 */
public class GeoMap extends Node {

	private static final Logger log = LoggerFactory.getLogger(GeoMap.class);
	public static final float COLLISION_CHECK_Z_OFFSET = 1;
	private static final float COLLISION_BOUND_OFFSET = 0.5f;
	private static final int NODE_CHUNK_SIZE = 256;

	private Terrain terrain;
	private final Map<Integer, Node> chunkById = new HashMap<>();

	private final Map<Integer, DespawnableNode> despawnables = new HashMap<>();
	private final Map<Integer, List<DespawnableNode>> despawnableTownObjects = new HashMap<>();
	private final Map<Integer, DespawnableNode> despawnableHouseDoors = new HashMap<>();
	private final Map<Integer, DespawnableNode[]> despawnableDoors = new HashMap<>();
	private final int mapId;

	public GeoMap(int mapId) {
		super(null);
		this.mapId = mapId;
	}

	public int getMapId() {
		return mapId;
	}

	@Override
	public int attachChild(Spatial child) {
		if (child instanceof DespawnableNode desp) {
			switch (desp.type) {
				case EVENT: // event object
					break;
				case PLACEABLE: // placeable
					despawnables.put(desp.id, desp);
					break;
				case HOUSE: // house
					break;
				case HOUSE_DOOR: // house door
					despawnableHouseDoors.put(desp.id, desp);
					break;
				case TOWN_OBJECT: // town object
					despawnableTownObjects.computeIfAbsent(desp.id, k -> new ArrayList<>()).add(desp);
					break;
				case DOOR_STATE1: // normal door state 1 (closed)
				case DOOR_STATE2: // normal door state 2 (opened)
					DespawnableNode[] doorStates = despawnableDoors.computeIfAbsent(desp.id, k -> new DespawnableNode[2]);
					doorStates[desp.type == DespawnableType.DOOR_STATE1 ? 0 : 1] = desp;
					break;
				default:
					throw new IllegalArgumentException(desp.type + " is not implemented");
			}
		}
		getOrCreateChunk(child).attachChild(child);
		return 0;
	}

	public boolean hasTerrain() {
		return terrain != null;
	}

	public boolean hasTerrainMaterials() {
		return terrain != null && terrain.hasMaterials();
	}

	public void setTerrain(Terrain terrain) {
		this.terrain = terrain;
	}

	private Node getOrCreateChunk(Spatial child) {
		int chunkId = RegionUtil.get2DRegionId(NODE_CHUNK_SIZE, child.getWorldBound().getCenter().x, child.getWorldBound().getCenter().y);
		Node node = chunkById.get(chunkId);
		if (node == null) {
			node = new Node("");
			chunkById.put(chunkId, node);
			super.attachChild(node);
		}
		return node;
	}

	public int getEntityCount() {
		return chunkById.values().stream().mapToInt(m->m.getChildren().size()).sum();
	}

	/**
	 * @return The surface Z coordinate nearest to the given zMax value at the given position or {@link Float#NaN} if not found / less than zMin.
	 */
	public float getZ(float x, float y, float zMax, float zMin, int instanceId) {
		return getZ(x, y, zMax, zMin, instanceId, false);
	}

	/**
	 * @return The surface Z coordinate nearest to the given zMax value at the given position or {@link Float#NaN} if not found / less than zMin.
	 * Also returns {@link Float#NaN} if ignoreSlopingSurface is true and the surface angle is >45° (too steep to safely stand on).
	 */
	public float getZ(float x, float y, float zMax, float zMin, int instanceId, boolean ignoreSlopingSurface) {
		CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), instanceId);
		results.setInvalidateSlopingSurface(ignoreSlopingSurface);
		Vector3f origin = new Vector3f(x, y, zMax);
		Vector3f target = new Vector3f(x, y, zMin);
		target.subtractLocal(origin).normalizeLocal(); // convert to direction vector
		Ray r = new Ray(origin, target);
		r.setLimit(zMax - zMin);
		collideWith(r, results);
		if (terrain != null)
			terrain.collideAtOrigin(r, results);
		CollisionResult closestCollision = results.getClosestCollision();
		return closestCollision == null ? Float.NaN : closestCollision.getContactPoint().z;
	}

	public Vector3f getClosestCollision(float x, float y, float z, float targetX, float targetY, float targetZ, boolean atNearGroundZ, int instanceId,
		byte intentions, IgnoreProperties ignoreProperties) {
		Vector3f origin = new Vector3f(x, y, z + COLLISION_CHECK_Z_OFFSET);
		CollisionResult closestCollision = getCollisions(origin, targetX, targetY, targetZ + COLLISION_CHECK_Z_OFFSET, instanceId, intentions, ignoreProperties).getClosestCollision();
		if (closestCollision == null) {
			Vector3f end = new Vector3f(targetX, targetY, targetZ);
			if (atNearGroundZ) {
				float geoZ = getZ(end.x, end.y, end.z + 1, end.z - 2, instanceId);
				if (!Float.isNaN(geoZ))
					end.z = geoZ;
			}
			return end;
		} else if (closestCollision.getDistance() <= COLLISION_BOUND_OFFSET + 0.05f) { // avoid climbing steep hills or passing through walls
			return new Vector3f(x, y, z);
		}
		Vector3f contactPoint = closestCollision.getContactPoint();
		applyCollisionCheckOffsets(contactPoint, origin, instanceId);
		return contactPoint;
	}

	private void applyCollisionCheckOffsets(Vector3f pos, Vector3f direction, int instanceId) {
		applyCollisionCheckOffsets(pos, direction, instanceId, false);
	}

	private void applyCollisionCheckOffsets(Vector3f pos, Vector3f direction, int instanceId, boolean allowNaN) {
		if (direction != null) {
			Vector3f dir = pos.subtract(direction).normalizeLocal();
			pos.subtractLocal(dir.multLocal(COLLISION_BOUND_OFFSET)); // set contact point back for proper ground calculation
			float geoZ = getZ(pos.x, pos.y, pos.z, pos.z - COLLISION_CHECK_Z_OFFSET * 3, instanceId);
			if (allowNaN || !Float.isNaN(geoZ)) {
				pos.z = geoZ;
			} else {
				pos.z -= COLLISION_CHECK_Z_OFFSET;
			}
		} else {
			pos.z -= COLLISION_CHECK_Z_OFFSET;
		}
	}

	public Vector3f findMovementCollision(Vector3f origin, float targetX, float targetY, int instanceId) {
		// check if we have an obstacle 1m in target direction
		origin.setZ(origin.getZ() + COLLISION_CHECK_Z_OFFSET);
		Vector2f targetXY = new Vector2f(targetX, targetY);
		Vector2f xyOffset = targetXY.subtract(origin.getX(), origin.getY()).normalizeLocal().multLocal(COLLISION_CHECK_Z_OFFSET);
		float nextX = origin.getX() + xyOffset.getX(), nextY = origin.getY() + xyOffset.getY();
		if (xyOffset.getX() >= 0 && nextX > targetX || xyOffset.getX() < 0 && nextX < targetX)
			nextX = targetX;
		if (xyOffset.getY() >= 0 && nextY > targetY || xyOffset.getY() < 0 && nextY < targetY)
			nextY = targetY;
		if (origin.getX() != nextX || origin.getY() != nextY) {
			CollisionResult closestCollision = getCollisions(origin, nextX, nextY, origin.getZ(), instanceId, CollisionIntention.DEFAULT_COLLISIONS.getId(), IgnoreProperties.ANY_RACE).getClosestCollision();
			if (closestCollision != null) { // obstacle found within 1m in target direction, return 0.5m offset position or origin of there's no ground
				Vector3f targetPoint = closestCollision.getContactPoint();
				applyCollisionCheckOffsets(targetPoint, origin, instanceId, true);
				if (!Float.isNaN(targetPoint.getZ()))
					return targetPoint;
			} else { // no obstacle 1m in target direction, now check if there's ground to stand on
				float geoZ = getZ(nextX, nextY, origin.getZ(), origin.getZ() - COLLISION_CHECK_Z_OFFSET * 2.5f, instanceId, true);
				if (!Float.isNaN(geoZ)) // there is ground, so we set our origin to the 1m offset position and start over
					return findMovementCollision(origin.set(nextX, nextY, geoZ), targetX, targetY, instanceId);
			}
		}
		return origin.setZ(origin.getZ() - COLLISION_CHECK_Z_OFFSET);
	}

	public CollisionResults getCollisions(float x, float y, float z, float targetX, float targetY, float targetZ, int instanceId, byte intentions, IgnoreProperties ignoreProperties) {
		return getCollisions(new Vector3f(x, y, z), targetX, targetY, targetZ, instanceId, intentions, ignoreProperties);
	}

	public CollisionResults getCollisions(Vector3f origin, float targetX, float targetY, float targetZ, int instanceId, byte intentions, IgnoreProperties ignoreProperties) {
		CollisionResults results = new CollisionResults(intentions, instanceId, ignoreProperties);
		Vector3f target = new Vector3f(targetX, targetY, targetZ);
		float limit = origin.distance(target);
		target.subtractLocal(origin).normalizeLocal(); // convert to direction vector
		Ray r = new Ray(origin, target);
		r.setLimit(limit);
		if (terrain != null) {
			terrain.collide(r, targetX, targetY, results);
		}
		collideWith(r, results);
		return results;
	}

	public boolean canSee(float x, float y, float z, float targetX, float targetY, float targetZ, int instanceId, IgnoreProperties ignoreProperties) {
		Vector3f origin = new Vector3f(x, y, z);
		Vector3f target = new Vector3f(targetX, targetY, targetZ);
		float distance = origin.distance(target);
		if (distance > 80f)
			return false;
		target.subtractLocal(origin).normalizeLocal(); // convert to direction vector
		Ray ray = new Ray(origin, target);
		ray.setLimit(distance);
		if (terrain != null && terrain.collide(ray, targetX, targetY, null))
			return false;
		CollisionResults results = new CollisionResults(CollisionIntention.CANT_SEE_COLLISIONS.getId(), instanceId, true, ignoreProperties);
		return collideWith(ray, results) == 0;
	}

	/**
	 * @return The terrain materialId at given position if no obstacle is in between, otherwise 0
	 */
	public int getTerrainMaterialAt(float x, float y, float z, int instanceId) {
		int matId = terrain == null ? 0 : terrain.getTerrainMaterialAt(x, y);
		if (matId > 0) {
			CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), instanceId);
			float zMax = z + 1;
			float zMin = z - 1;
			Vector3f origin = new Vector3f(x, y, zMax);
			Vector3f target = new Vector3f(x, y, zMin);
			target.subtractLocal(origin).normalizeLocal(); // convert to direction vector
			Ray r = new Ray(origin, target);
			r.setLimit(zMax - zMin);
			terrain.collideAtOrigin(r, results);
			CollisionResult terrainCollision = results.getClosestCollision();
			if (terrainCollision != null && (collideWith(r, results) == 0 || results.getClosestCollision().equals(terrainCollision))) {
				return matId;
			}
		}
		return 0;
	}

	public void spawnPlaceableObject(int instanceId, int staticId) {
		DespawnableNode node = despawnables.get(staticId);
		if (node != null) {
			node.setActive(instanceId, true);
		}
	}

	public void despawnPlaceableObject(int instanceId, int staticId) {
		DespawnableNode node = despawnables.get(staticId);
		if (node != null) {
			node.setActive(instanceId, false);
		}
	}

	public void updateTownToLevel(int townId, int level) {
		if (despawnableTownObjects.containsKey(townId) && !despawnableTownObjects.get(townId).isEmpty()) {
			for (DespawnableNode despawnableNode : despawnableTownObjects.get(townId)) {
				int levelBitMask = 1 << (level - 1);
				despawnableNode.setActive(1, (despawnableNode.levelBitMask & levelBitMask) != 0);
			}
		}
	}

	public void setHouseDoorState(int instanceId, int houseAddress, HouseDoorState state) {
		DespawnableNode houseDoor = despawnableHouseDoors.get(houseAddress);
		if (houseDoor != null)
			houseDoor.setActive(instanceId, state != HouseDoorState.OPEN);
	}

	public void setDoorState(int instanceId, int doorId, boolean open) {
		DespawnableNode[] doors = despawnableDoors.get(doorId);
		if (doors == null) {
			if (GeoDataConfig.GEO_ENABLE && !getIgnorableDoorIds().contains(doorId))
				log.warn("No geometry found for door " + doorId + " in world " + mapId);
		} else {
			if (doors[0] != null) {
				doors[0].setActive(instanceId, !open);
			} else {
				log.warn("Door state 1 not available for door " + doorId + " in world " + mapId);
			}
			if (doors[1] != null) {
				doors[1].setActive(instanceId, open);
			} else {
				log.warn("Door state 2 not available for door " + doorId + " in world " + mapId);
			}
		}
	}

	private Set<Integer> getIgnorableDoorIds() {
		return switch (WorldMapType.getWorld(mapId)) {
			// TODO mesh is excluded on purpose in geobuilder due to incorrect collision data: objects/npc/level_object/idyun_bridge/idyun_bridge_01a.cga
			case RENTUS_BASE, OCCUPIED_RENTUS_BASE -> Set.of(145);
			// all of the following doors have no collision mesh in the game client (you can walk right through them)
			case ABYSSAL_SPLINTER, UNSTABLE_SPLINTER -> Set.of(15, 16, 18, 69);
			case ATURAM_SKY_FORTRESS -> Set.of(128, 138, 308, 307);
			case ESOTERRACE -> Set.of(78);
			case Test_MRT_IDZone -> Set.of(49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 73);
			case RAKSANG_RUINS -> Set.of(219);
			case KAMAR_BATTLEFIELD -> Set.of(5, 144);
			default -> Set.of();
		};
	}

	public Stream<Geometry> getGeometries() {
		return getGeometries(getChildren());
	}

	private static Stream<Geometry> getGeometries(List<Spatial> spatials) {
		return spatials.stream().mapMulti((child, consumer) -> {
			if (child instanceof Geometry geometry)
				consumer.accept(geometry);
			else if (child instanceof Node node)
				getGeometries(node.getChildren()).forEach(consumer);
		});
	}
}
