package com.aionemu.gameserver.geoEngine.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.bounding.BoundingSphere;
import com.aionemu.gameserver.geoEngine.bounding.BoundingVolume;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Geometry;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.geoEngine.scene.mesh.DoorGeometry;

/**
 * @author Mr. Poke
 */
public class GeoMap extends Node {

	private static final Logger log = LoggerFactory.getLogger(GeoMap.class);
	public static final float MAX_Z = 4000;
	public static final float MIN_Z = 0;

	private short[] terrainData;
	private int terrainDataRows, terrainDataCols;
	private List<BoundingBox> tmpBox = new ArrayList<>();
	private Map<String, List<DoorGeometry>> doors = new HashMap<>();

	public GeoMap(String name, int worldSize) {
		setCollisionFlags((short) (CollisionIntention.ALL.getId() << 8));
		for (int x = 0; x < worldSize; x += 256) {
			for (int y = 0; y < worldSize; y += 256) {
				Node geoNode = new Node("");
				geoNode.setCollisionFlags((short) (CollisionIntention.ALL.getId() << 8));
				tmpBox.add(new BoundingBox(new Vector3f(x, y, MIN_Z), new Vector3f(x + 256, y + 256, MAX_Z)));
				super.attachChild(geoNode);
			}
		}
	}

	public DoorGeometry getDoor(int worldId, String meshFile, float x, float y, float z) {
		Vector3f templatePoint = new Vector3f(x, y, z);
		List<DoorGeometry> doors = this.doors.get(meshFile.toLowerCase());
		DoorGeometry nearestMatch = null;
		if (doors != null) {
			for (DoorGeometry door : doors) {
				if (door.getWorldBound().intersects(templatePoint))
					return door;
			}
			nearestMatch = findNearestMatch(doors, templatePoint);
		}
		String spawnPoint = toTemplateCoords(templatePoint);
		if (nearestMatch == null)
			log.warn("Could not find static door: " + worldId + " " + meshFile + " " + spawnPoint);
		else
			log.warn("Static door: " + worldId + " " + meshFile + " " + spawnPoint + " should spawn at " + toSpawnPoint(nearestMatch.getWorldBound()));
		return nearestMatch;
	}

	private DoorGeometry findNearestMatch(List<DoorGeometry> doors, Vector3f pos) {
		DoorGeometry nearestMatch = null;
		float nearestDist = 15;
		for (DoorGeometry door : doors) {
			float dist = door.getWorldBound().distanceTo(pos);
			if (dist < nearestDist) {
				nearestMatch = door;
				nearestDist = dist;
			}
		}
		return nearestMatch;
	}

	private String toTemplateCoords(Vector3f coords) {
		return "x=\"" + coords.getX() + "\" y=\"" + coords.getY() + "\" z=\"" + coords.getZ() + "\"";
	}

	private String toSpawnPoint(BoundingVolume boundingVolume) {
		Vector3f spawnPosition = new Vector3f(boundingVolume.getCenter());
		float zOffset = 0; // boundingVolume.center is always the middle point of the mesh, we need to subtract a z offset to find the ground spawn pos
		if (boundingVolume instanceof BoundingBox)
			zOffset = ((BoundingBox) boundingVolume).getZExtent();
		else if (boundingVolume instanceof BoundingSphere)
			zOffset = ((BoundingSphere) boundingVolume).getRadius();
		zOffset -= 0.01f;
		if (zOffset > 0)
			spawnPosition.setZ(spawnPosition.getZ() - zOffset);
		return toTemplateCoords(spawnPosition);
	}

	@Override
	public int attachChild(Spatial child) {
		int i = 0;

		if (child instanceof DoorGeometry) {
			int index = child.getName().lastIndexOf('/');
			String meshFileName = child.getName().substring(index + 1).toLowerCase();
			doors.computeIfAbsent(meshFileName, (k) -> new ArrayList<>()).add((DoorGeometry) child);
		}

		for (Spatial spatial : getChildren()) {
			if (tmpBox.get(i).intersects(child.getWorldBound())) {
				((Node) spatial).attachChild(child);
			}
			i++;
		}
		return 0;
	}

	/**
	 * @param terrainData
	 *          The terrainData to set.
	 */
	public void setTerrainData(short[] terrainData) {
		this.terrainData = terrainData;
		terrainDataRows = terrainDataCols = (int) Math.sqrt(terrainData.length);
	}

	/**
	 * @return The surface Z coordinate nearest to the given zMax value at the given position or {@link Float#NaN} if not found / less than zMin.
	 */
	public float getZ(float x, float y, float zMax, float zMin, int instanceId) {
		CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), instanceId);
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
			Vector3f result = new Vector3f();
			if (terrainCollision(x, y, r, new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), result))
				terrain = result;
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

	public Vector3f getClosestCollision(float x, float y, float z, float targetX, float targetY, float targetZ, boolean atNearGroundZ, int instanceId,
		byte intentions) {
		int zOffset = 1; // check for collisions 1m above input z
		float collisionOffset = 0.5f; // collision points will be 0.5m in front of the real contact
		Vector3f origin = new Vector3f(x, y, z + zOffset);
		CollisionResult closestCollision = getCollisions(origin, targetX, targetY, targetZ + zOffset, instanceId, intentions).getClosestCollision();
		if (closestCollision == null) {
			Vector3f end = new Vector3f(targetX, targetY, targetZ);
			if (atNearGroundZ) {
				float geoZ = getZ(end.x, end.y, end.z + 1, end.z - 2, instanceId);
				if (!Float.isNaN(geoZ))
					end.z = geoZ;
			}
			return end;
		} else if (closestCollision.getDistance() <= collisionOffset + 0.05f) { // avoid climbing steep hills or passing through walls
			return new Vector3f(x, y, z);
		}
		Vector3f contactPoint = closestCollision.getContactPoint();
		if (atNearGroundZ) {
			Vector3f direction = contactPoint.subtract(origin).normalize();
			contactPoint.subtractLocal(direction.multLocal(collisionOffset)); // set contact point back for proper ground calculation
			float geoZ = getZ(contactPoint.x, contactPoint.y, contactPoint.z, contactPoint.z - 3, instanceId);
			if (!Float.isNaN(geoZ))
				contactPoint.z = geoZ;
			else
				contactPoint.z -= zOffset;
		} else {
			contactPoint.z -= zOffset;
		}

		return contactPoint;
	}

	public CollisionResults getCollisions(float x, float y, float z, float targetX, float targetY, float targetZ, int instanceId, byte intentions) {
		return getCollisions(new Vector3f(x, y, z), targetX, targetY, targetZ, instanceId, intentions);
	}

	private CollisionResults getCollisions(Vector3f origin, float targetX, float targetY, float targetZ, int instanceId, byte intentions) {
		CollisionResults results = new CollisionResults(intentions, instanceId);
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
			if (terrainCollision(curX, curY, ray, p1, p2, p3, p4, result))
				return result;
		}
		return null;
	}

	private boolean terrainCollision(float x, float y, Ray ray, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
		return terrainCollision(x, y, ray, p1, p2, p3, p4, null);
	}

	private boolean terrainCollision(float x, float y, Ray ray, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Vector3f result) {
		// z1┌───┐z2 │ top view where z1 and z2 face north. each corner represents a terrainData z coordinate around given x/y.
		//   │ ∕ │   │ the resolution of terrainData is 2x2m. this rectangle effectively consists of two adjacent triangles in 3d space,
		// z3└───┘z4 │ the point where our ray collides with one of those triangles is the exact terrain coordinate (meaning the map ground)
		int z1x = (int) (x / 2f);
		int z1y = (int) (y / 2f);
		float z1, z2, z3, z4;
		if (terrainData.length == 1) {
			z1 = z2 = z3 = z4 = terrainData[0] / 32f;
		} else {
			if (isOutsideValidBounds(z1x, z1y))
				return false;
			int z1Index = z1y + (z1x * terrainDataRows);
			int z3Index = z1y + ((z1x + 1) * terrainDataRows);
			if (z3Index + 1 >= terrainData.length)
				return false;
			z1 = terrainData[z1Index] / 32f;
			z2 = terrainData[z1Index + 1] / 32f;
			z3 = terrainData[z3Index] / 32f;
			z4 = terrainData[z3Index + 1] / 32f;
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
					if (ray.intersectWhere(p1, p2, new Vector3f(xMin, yMax, z2), result) || ray.intersectWhere(p1, p3, new Vector3f(xMax, yMin, z3), result))
						return true;
				} else if (p2 == overlappingPoint) { // old p2 overlaps with new p3
					if (ray.intersectWhere(p2, p1, new Vector3f(xMin, yMin, z1), result) || ray.intersectWhere(p2, p4, new Vector3f(xMax, yMax, z4), result))
						return true;
				} else if (p3 == overlappingPoint) { // old p3 overlaps with new p2
					if (ray.intersectWhere(p3, p1, new Vector3f(xMin, yMin, z1), result) || ray.intersectWhere(p3, p4, new Vector3f(xMax, yMax, z4), result))
						return true;
				} else if (p4 == overlappingPoint) { // old p4 overlaps with new p1
					if (ray.intersectWhere(p4, p2, new Vector3f(xMin, yMax, z2), result) || ray.intersectWhere(p4, p3, new Vector3f(xMax, yMin, z3), result))
						return true;
				}
			}
		}
		p1.set(xMin, yMin, z1);
		p2.set(xMin, yMax, z2);
		p3.set(xMax, yMin, z3);
		p4.set(xMax, yMax, z4);
		return ray.intersectWhere(p1, p2, p3, result) || ray.intersectWhere(p4, p2, p3, result); // test if ray intersects the triangle parts of our rectangle
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

	public boolean canSee(float x, float y, float z, float targetX, float targetY, float targetZ, Geometry targetGeometry, int instanceId) {
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
		CollisionResults results = new CollisionResults(CollisionIntention.DEFAULT_COLLISIONS.getId(), instanceId, true, targetGeometry);
		results.setCanSeeCheck(true);
		return collideWith(ray, results) == 0;
	}

	@Override
	public void updateModelBound() {
		if (getChildren() != null) {
			getChildren().removeIf(s -> s instanceof Node && ((Node) s).getChildren().isEmpty());
		}
		super.updateModelBound();
	}
}
