package com.aionemu.gameserver.geoEngine.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private static final float TERRAIN_POINTS_DISTANCE = 2f;
	private static final int NODE_CHUNK_SIZE = 256;

	private short[] terrainData = new short[] { 0 };
	private byte[] terrainMaterials;
	private int terrainDataRows, terrainDataCols;
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

	public void setTerrainData(short[] terrainData) {
		this.terrainData = terrainData;
		terrainDataRows = terrainDataCols = (int) Math.sqrt(terrainData.length);
	}

	public void setTerrainMaterials(byte[] terrainMaterials) {
		this.terrainMaterials = terrainMaterials;
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
		Vector3f terrain = null;
		if (terrainData.length == 1) {
			if (terrainData[0] != 0)
				terrain = new Vector3f(x, y, terrainData[0] / 32f);
		} else {
			Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), p4 = new Vector3f(), result = new Vector3f();
			if (terrainCollision(x, y, r, p1, p2, p3, p4, result)) {
				terrain = result;
				if (ignoreSlopingSurface && getMaximumHeightDiff(p1, p2, p3, p4) > TERRAIN_POINTS_DISTANCE) // height diff >2m means >45° elevation
					terrain.setZ(Float.NaN);
			}
		}
		if (terrain != null && terrain.z >= zMin && terrain.z <= zMax) {
			CollisionResult result = new CollisionResult(terrain, zMax - terrain.z);
			results.addCollision(result);
		}
		if (results.size() == 0) {
			return Float.NaN;
		}
		return results.getClosestCollision().getContactPoint().z;
	}

	private float getMaximumHeightDiff(Vector3f vector1, Vector3f... vectors) {
		float maxZ = vector1.getZ(), minZ = vector1.getZ();
		for (Vector3f vector3f : vectors) {
			if (Float.isNaN(maxZ) || vector3f.getZ() > maxZ)
				maxZ = vector3f.getZ();
			if (Float.isNaN(minZ) || vector3f.getZ() < minZ)
				minZ = vector3f.getZ();
		}
		return maxZ - minZ;
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
		Vector3f terrain = calculateTerrainCollision(origin.x, origin.y, targetX, targetY, r);
		if (terrain != null) {
			CollisionResult result = new CollisionResult(terrain, terrain.distance(origin));
			results.addCollision(result);
		}

		collideWith(r, results);
		return results;
	}

	private Vector3f calculateTerrainCollision(float x, float y, float targetX, float targetY, Ray ray) {
		float distanceX = targetX - x;
		float distanceY = targetY - y;
		float maxDistance = Math.abs(ray.getLimit());

		Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), p4 = new Vector3f(), result = new Vector3f();
		for (int curDistance = 0; curDistance < maxDistance; curDistance += 2) {
			float distanceFactor = curDistance / ray.getLimit();
			float curX = x + distanceX * distanceFactor;
			float curY = y + distanceY * distanceFactor;
			if (terrainCollision(curX, curY, ray, p1, p2, p3, p4, result)) {
				return result;
			}
		}
		return null;
	}

	private boolean terrainCollision(float x, float y, Ray ray, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
		return terrainCollision(x, y, ray, p1, p2, p3, p4, null);
	}

	private boolean terrainCollision(float x, float y, Ray ray, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f result) {
		// z1┌───┐z2 top view where z1 and z2 face north. each corner represents a terrainData z coordinate around given x/y.
		// · │ ∕ │ · the resolution of terrainData is 2x2m. this rectangle effectively consists of two adjacent triangles in 3d space,
		// z3└───┘z4 the point where our ray collides with one of those triangles is the exact terrain coordinate (meaning the map ground)
		int z1x = (int) (x / TERRAIN_POINTS_DISTANCE);
		int z1y = (int) (y / TERRAIN_POINTS_DISTANCE);
		float z1, z2, z3, z4;
		if (terrainData.length == 1) {
			z1 = z2 = z3 = z4 = terrainData[0] == Short.MIN_VALUE ? Float.NaN : (terrainData[0] / 32f);
		} else {
			if (isOutsideValidBounds(z1x, z1y))
				return false;
			int z1Index = z1y + (z1x * terrainDataRows);
			int z3Index = z1y + ((z1x + 1) * terrainDataRows);
			if (z3Index + 1 >= terrainData.length)
				return false;
			z1 = terrainData[z1Index] == Short.MIN_VALUE ? Float.NaN : (terrainData[z1Index] / 32f);
			z2 = terrainData[z1Index + 1] == Short.MIN_VALUE ? Float.NaN : (terrainData[z1Index + 1] / 32f);
			z3 = terrainData[z3Index] == Short.MIN_VALUE ? Float.NaN : (terrainData[z3Index] / 32f);
			z4 = terrainData[z3Index + 1] == Short.MIN_VALUE ? Float.NaN : (terrainData[z3Index + 1] / 32f);
		}
		int xMin = z1x * 2; // x coord for z1 & z2 (top of the rectangle)
		float xMax = xMin + 2; // x coord for z3 & z4 (bottom of the rectangle)
		int yMin = z1y * 2; // y coord for z1 & z3 (left of the rectangle)
		float yMax = yMin + 2; // y coord for z2 & z4 (right of the rectangle)
		if (!p1.equals(Vector3f.ZERO)) { // skip first call (p1-p4 have no data yet)
			Vector3f overlappingPoint = findSingleOverlappingPoint(xMin, xMax, yMin, yMax, z1, z2, z3, z4, p1, p2, p3, p4);
			// single overlap means that the old and new rectangle share one corner. this means we have a diagonal ray, so we check the adjacent triangles to
			// the old and new rectangle
			if (overlappingPoint != null) {
				if (p1 == overlappingPoint) { // old p1 overlaps with new p4
					if (!Float.isNaN(z2) && ray.intersectWhere(p1, p2, new Vector3f(xMin, yMax, z2), result) ||
							!Float.isNaN(z3) && ray.intersectWhere(p1, p3, new Vector3f(xMax, yMin, z3), result))
						return true;
				} else if (p2 == overlappingPoint) { // old p2 overlaps with new p3
					if (!Float.isNaN(z1) && ray.intersectWhere(p2, p1, new Vector3f(xMin, yMin, z1), result) ||
							!Float.isNaN(z4) && ray.intersectWhere(p2, p4, new Vector3f(xMax, yMax, z4), result))
						return true;
				} else if (p3 == overlappingPoint) { // old p3 overlaps with new p2
					if (!Float.isNaN(z1) && ray.intersectWhere(p3, p1, new Vector3f(xMin, yMin, z1), result) ||
							!Float.isNaN(z4) && ray.intersectWhere(p3, p4, new Vector3f(xMax, yMax, z4), result))
						return true;
				} else if (p4 == overlappingPoint) { // old p4 overlaps with new p1
					if (!Float.isNaN(z2) && ray.intersectWhere(p4, p2, new Vector3f(xMin, yMax, z2), result) ||
							!Float.isNaN(z3) && ray.intersectWhere(p4, p3, new Vector3f(xMax, yMin, z3), result))
						return true;
				}
			}
		}
		p1.set(xMin, yMin, z1);
		p2.set(xMin, yMax, z2);
		p3.set(xMax, yMin, z3);
		p4.set(xMax, yMax, z4);
		if (!Float.isNaN(z2) && !Float.isNaN(z3)) {
			return (!Float.isNaN(z1) && ray.intersectWhere(p1, p2, p3, result) ||
					!Float.isNaN(z4) && ray.intersectWhere(p4, p2, p3, result));
		} else {
			return false;
		}
	}

	private boolean isOutsideValidBounds(int x, int y) {
		return x < 0 || y < 0 || x >= terrainDataRows || y >= terrainDataCols;
	}

	/**
	 * @return The only point which has one of the given coordinates. If none or more than one match, null will be returned.
	 */
	private Vector3f findSingleOverlappingPoint(float xMin, float xMax, float yMin, float yMax, float z1, float z2, float z3, float z4,
		Vector3f... points) {
		Vector3f singleMatch = null;
		Vector3f comparator = new Vector3f(xMin, yMin, z1);
		for (Vector3f point : points) {
			if (comparator.equals(point)) {
				singleMatch = point;
				break;
			}
		}
		comparator.set(xMin, yMax, z2);
		for (Vector3f point : points) {
			if (comparator.equals(point)) {
				if (singleMatch != null)
					return null;
				singleMatch = point;
				break;
			}
		}
		comparator.set(xMax, yMin, z3);
		for (Vector3f point : points) {
			if (comparator.equals(point)) {
				if (singleMatch != null)
					return null;
				singleMatch = point;
				break;
			}
		}
		comparator.set(xMax, yMax, z4);
		for (Vector3f point : points) {
			if (comparator.equals(point)) {
				if (singleMatch != null)
					return null;
				singleMatch = point;
				break;
			}
		}
		return singleMatch;
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
		float distanceX = x - targetX;
		float distanceY = y - targetY;
		float distance2d = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
		Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f(), p4 = new Vector3f();
		for (int curDistance = 2; curDistance < distance2d; curDistance += 2) {
			float distanceFactor = curDistance / distance2d;
			float curX = targetX + distanceX * distanceFactor;
			float curY = targetY + distanceY * distanceFactor;
			if (terrainCollision(curX, curY, ray, p1, p2, p3, p4))
				return false;
		}
		CollisionResults results = new CollisionResults(CollisionIntention.CANT_SEE_COLLISIONS.getId(), instanceId, true, ignoreProperties);
		return collideWith(ray, results) == 0;
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
				despawnableNode.setActive(1, despawnableNode.level == (byte) level);
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
			// TODO mesh is excluded on purpose in geobuilder due to incorrect collision data: objects/npc/level_object/idyun_bridge/idyun_bridge_01a.cga
			if (!GeoDataConfig.GEO_ENABLE || doorId == 145 && (mapId == WorldMapType.OCCUPIED_RENTUS_BASE.getId() || mapId == WorldMapType.RENTUS_BASE.getId()))
				return;
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

	public boolean hasTerrainMaterials() {
		return terrainMaterials != null;
	}

	/**
	 * @return The terrain materialId at position x, y if no obstacle is in between, otherwise 0
	 */
	public int getTerrainMaterialAt(float x, float y, float z, int instanceId) {
		int mat1x = (int) (x / TERRAIN_POINTS_DISTANCE);
		int mat1y = (int) (y / TERRAIN_POINTS_DISTANCE);
		if (isOutsideValidBounds(mat1x, mat1y))
			return 0;
		int width = (int) Math.sqrt(terrainMaterials.length);
		int mat1Index = mat1y + (mat1x * width);
		int mat3Index = mat1y + ((mat1x + 1) * width);
		int matId = 0;
		// check whether triangle points p1, p2, p3 have materials assigned
		if (terrainMaterials[mat1Index] != 0 && terrainMaterials[mat1Index] == terrainMaterials[mat1Index + 1]  && terrainMaterials[mat1Index]  == terrainMaterials[mat3Index]) {
			if (isLeft((mat1x * 2) + 2, mat1y * 2, mat1x * 2, (mat1y * 2) + 2, x, y)) { // check if x, y is in triangle
				matId = terrainMaterials[mat1Index] & 0xFF;
			}
		}
		if (matId == 0 && (mat3Index + 1) < terrainMaterials.length && terrainMaterials[mat3Index + 1] != 0 && terrainMaterials[mat3Index + 1] == terrainMaterials[mat3Index] && terrainMaterials[mat3Index + 1] == terrainMaterials[mat1Index + 1]) { // check whether triangle points p2, p3, p4 have materials assigned
			if (!isLeft((mat1x * 2) + 2, mat1y * 2, mat1x * 2, (mat1y * 2) + 2, x, y)) { // check if x, y is in triangle
				matId = terrainMaterials[mat3Index + 1] & 0xFF;
			}
		}

		if (matId > 0) {
			CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), instanceId);
			float zMax = z + 1;
			float zMin = z - 1;
			Vector3f origin = new Vector3f(x, y, zMax);
			Vector3f target = new Vector3f(x, y, zMin);
			target.subtractLocal(origin).normalizeLocal(); // convert to direction vector
			Ray r = new Ray(origin, target);
			r.setLimit(zMax - zMin);
			Vector3f terrain = null;
			if (terrainData.length == 1) {
				if (terrainData[0] != 0)
					terrain = new Vector3f(x, y, terrainData[0] / 32f);
			} else {
				Vector3f result = new Vector3f();
				if (terrainCollision(x, y, r, new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), result))
					terrain = result;
			}
			if (terrain != null && terrain.z >= zMin && terrain.z <= zMax) {
				CollisionResult result = new CollisionResult(terrain, zMax - terrain.z);
				results.addCollision(result);
				collideWith(r, results);
				if (results.getClosestCollision().equals(result)) {
					return matId;
				}
			}
			matId = 0;
		}
		return matId;
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

	/**
	 * @return True if (targetX, targetY) is left of the line made by (startX, startY) -> (endX, endY)
	 */
	private boolean isLeft(float startX, float startY, float endX, float endY, float targetX, float targetY){
		return ((endX - startX)*(targetY - startY) - (endY - startY)*(targetX - startX)) > 0;
	}
}
