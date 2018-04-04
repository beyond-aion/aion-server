package com.aionemu.gameserver.geoEngine.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
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
	private List<BoundingBox> tmpBox = new ArrayList<>();
	private Map<String, DoorGeometry> doors = new HashMap<>();

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

	public String getDoorName(int worldId, String meshFile, float x, float y, float z) {
		String mesh = meshFile.toUpperCase();
		Vector3f templatePoint = new Vector3f(x, y, z);
		float distance = Float.MAX_VALUE;
		DoorGeometry foundDoor = null;
		for (Entry<String, DoorGeometry> door : doors.entrySet()) {
			if (!(door.getKey().startsWith(Integer.toString(worldId)) && door.getKey().endsWith(mesh)))
				continue;
			DoorGeometry checkDoor = doors.get(door.getKey());
			float doorDistance = checkDoor.getWorldBound().distanceTo(templatePoint);
			if (distance > doorDistance) {
				distance = doorDistance;
				foundDoor = checkDoor;
			}
			if (checkDoor.getWorldBound().intersects(templatePoint)) {
				foundDoor = checkDoor;
				break;
			}
		}
		if (foundDoor == null) {
			log.warn("Could not find static door: " + worldId + " " + meshFile + " " + templatePoint);
			return null;
		}
		foundDoor.setFoundTemplate(true);
		// log.info("Static door " + worldId + " " + meshFile + " " + templatePoint + " matched " + foundDoor.getName() +
		// "; distance: " + distance);
		return foundDoor.getName();
	}

	public void setDoorState(int instanceId, String name, boolean isOpened) {
		DoorGeometry door = doors.get(name);
		if (door != null)
			door.setDoorState(instanceId, isOpened);
	}

	@Override
	public int attachChild(Spatial child) {
		int i = 0;

		if (child instanceof DoorGeometry)
			doors.put(child.getName(), (DoorGeometry) child);

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
	}

	/**
	 * @return The surface Z coordinate nearest to the given zMax value at the given position or {@link Float#NaN} if not found / less than zMin.
	 */
	public float getZ(float x, float y, float zMax, float zMin, int instanceId) {
		CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), false, instanceId);
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
		} else
			terrain = terrainCollision(x, y, r);
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
		CollisionResults results = new CollisionResults(intentions, false, instanceId);
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
		float x2 = targetX - x;
		float y2 = targetY - y;
		int intD = (int) Math.abs(ray.getLimit());

		for (float s = 0; s < intD; s += 2) {
			float tempX = x + (x2 * s / ray.getLimit());
			float tempY = y + (y2 * s / ray.getLimit());
			Vector3f result = terrainCollision(tempX, tempY, ray);
			if (result != null)
				return result;
		}
		return null;
	}

	private Vector3f terrainCollision(float x, float y, Ray ray) {
		y /= 2f;
		x /= 2f;
		int xInt = (int) x;
		int yInt = (int) y;
		// z1┌───┐z2
		//   │ ∕ │ (top view, each point represents a z coordinate around given x/y, this "rectangle" consists of two adjacent triangles in 3d space)
		// z3└───┘z4
		float z1, z2, z3, z4;
		if (terrainData.length == 1) {
			z1 = z2 = z3 = z4 = terrainData[0] / 32f;
		} else {
			int size = (int) Math.sqrt(terrainData.length);
			try {
				int i1 = yInt + (xInt * size);
				int i2 = yInt + ((xInt + 1) * size);
				z1 = terrainData[i1] / 32f;
				z2 = terrainData[i1 + 1] / 32f;
				z3 = terrainData[i2] / 32f;
				z4 = terrainData[i2 + 1] / 32f;
			} catch (Exception e) {
				return null;
			}
		}
		if (z2 >= 0 && z3 >= 0) {
			Vector3f result = new Vector3f();
			Vector3f pointA = new Vector3f(xInt * 2, yInt * 2, z1);
			Vector3f pointB = new Vector3f(xInt * 2, (yInt + 1) * 2, z2);
			Vector3f pointC = new Vector3f((xInt + 1) * 2, yInt * 2, z3);
			if (z1 >= 0) {
				if (ray.intersectWhere(pointA, pointB, pointC, result))
					return result;
			}
			if (z4 >= 0) {
				pointA.set((xInt + 1) * 2, (yInt + 1) * 2, z4);
				if (ray.intersectWhere(pointA, pointB, pointC, result))
					return result;
			}
		}
		return null;
	}

	public boolean canSee(float x, float y, float z, float targetX, float targetY, float targetZ, int instanceId) {
		Vector3f origin = new Vector3f(x, y, z);
		Vector3f target = new Vector3f(targetX, targetY, targetZ);
		float distance = origin.distance(target);
		if (distance > 80f)
			return false;
		target.subtractLocal(origin).normalizeLocal(); // convert to direction vector
		Ray r = new Ray(origin, target);
		r.setLimit(distance);
		float x2 = x - targetX;
		float y2 = y - targetY;
		float distance2d = (float) Math.sqrt(x2 * x2 + y2 * y2);
		for (float s = 2; s < distance2d; s += 2) {
			float tempX = targetX + (x2 * s / distance2d);
			float tempY = targetY + (y2 * s / distance2d);
			Vector3f result = terrainCollision(tempX, tempY, r);
			if (result != null)
				return false;
		}
		CollisionResults results = new CollisionResults(CollisionIntention.DEFAULT_COLLISIONS.getId(), true, instanceId);
		int collisions = collideWith(r, results);
		return results.size() == 0 && collisions == 0;
	}

	@Override
	public void updateModelBound() {
		if (getChildren() != null) {
			getChildren().removeIf(s -> s instanceof Node && ((Node) s).getChildren().isEmpty());
		}
		super.updateModelBound();
	}
}
