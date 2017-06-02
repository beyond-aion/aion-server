package com.aionemu.gameserver.geoEngine.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.aionemu.gameserver.geoEngine.math.Triangle;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.geoEngine.scene.mesh.DoorGeometry;

/**
 * @author Mr. Poke
 */
public class GeoMap extends Node {

	private static final Logger log = LoggerFactory.getLogger(GeoMap.class);

	private short[] terrainData;
	private List<BoundingBox> tmpBox = new ArrayList<>();
	private Map<String, DoorGeometry> doors = new HashMap<>();

	public GeoMap(String name, int worldSize) {
		setCollisionFlags((short) (CollisionIntention.ALL.getId() << 8));
		for (int x = 0; x < worldSize; x += 256) {
			for (int y = 0; y < worldSize; y += 256) {
				Node geoNode = new Node("");
				geoNode.setCollisionFlags((short) (CollisionIntention.ALL.getId() << 8));
				tmpBox.add(new BoundingBox(new Vector3f(x, y, 0), new Vector3f(x + 256, y + 256, 4000)));
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
	 * @return The highest found Z coordinate at the given position or {@link Float#NaN} if not found.
	 */
	public float getZ(float x, float y) {
		return getZ(x, y, 4000, 0, 1);
	}

	/**
	 * @return The Z coordinate nearest to the given Z value at the given position or {@link Float#NaN} if not found.
	 */
	public float getZ(float x, float y, float z, int instanceId) {
		return getZ(x, y, z + 2, z - 100, instanceId);
	}

	/**
	 * @return The surface Z coordinate nearest to the given zMax value at the given position or {@link Float#NaN} if not found / less than zMin.
	 */
	public float getZ(float x, float y, float zMax, float zMin, int instanceId) {
		CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), false, instanceId);
		Vector3f pos = new Vector3f(x, y, zMax);
		Vector3f dir = new Vector3f(x, y, zMin);
		dir.subtractLocal(pos).normalizeLocal();
		Ray r = new Ray(pos, dir);
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

	public Vector3f getClosestCollision(float x, float y, float z, float targetX, float targetY, float targetZ, boolean changeDirection, boolean fly,
		int instanceId, byte intentions) {
		float zChecked1 = 0;
		float zChecked2 = 0;
		if (!fly && changeDirection) {
			zChecked1 = z;
			z = getZ(x, y, z, instanceId);
		}
		Vector3f start = new Vector3f(x, y, z);
		Vector3f end = new Vector3f(targetX, targetY, targetZ);
		CollisionResults results = getCollisions(x, y, z + 1, targetX, targetY, targetZ + 1, instanceId, intentions);

		float geoZ = 0;
		if (results.size() == 0) {
			if (fly)
				return end;
			if (zChecked1 > 0 && targetX == x && targetY == y && targetZ == zChecked1)
				geoZ = z;
			else {
				zChecked2 = targetZ;
				geoZ = getZ(targetX, targetY, targetZ, instanceId);
			}
			if (Math.abs(geoZ - targetZ) < start.distance(end)) {
				return end.setZ(geoZ);
			}
			return start;
		}
		Vector3f contactPoint = results.getClosestCollision().getContactPoint();
		float distance = results.getClosestCollision().getDistance();
		if (distance < 1)
			return start;
		contactPoint.z -= 1; // -1m (offset from getCollisions call)
		if (!fly && changeDirection) {
			if (zChecked1 > 0 && contactPoint.x == x && contactPoint.y == y && contactPoint.z == zChecked1)
				contactPoint.z = z;
			else if (zChecked2 > 0 && contactPoint.x == targetX && contactPoint.y == targetY && contactPoint.z == zChecked2)
				contactPoint.z = geoZ;
			else
				contactPoint.z = getZ(contactPoint.x, contactPoint.y, contactPoint.z, instanceId);
		}
		if (!fly && Math.abs(start.z - contactPoint.z) > distance)
			return start;

		return contactPoint;
	}

	public CollisionResults getCollisions(float x, float y, float z, float targetX, float targetY, float targetZ, int instanceId, byte intentions) {
		Vector3f pos = new Vector3f(x, y, z);
		Vector3f dir = new Vector3f(targetX, targetY, targetZ);

		CollisionResults results = new CollisionResults(intentions, false, instanceId);

		float limit = pos.distance(dir);
		dir.subtractLocal(pos).normalizeLocal();
		Ray r = new Ray(pos, dir);
		r.setLimit(limit);
		Vector3f terrain = calculateTerrainCollision(x, y, targetX, targetY, r);
		if (terrain != null) {
			CollisionResult result = new CollisionResult(terrain, terrain.distance(pos));
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
		// p1-----p2
		// || ||
		// || ||
		// p3-----p4
		float p1, p2, p3, p4;
		if (terrainData.length == 1) {
			p1 = p2 = p3 = p4 = terrainData[0] / 32f;
		} else {
			int size = (int) Math.sqrt(terrainData.length);
			try {
				int i1 = yInt + (xInt * size);
				int i2 = yInt + ((xInt + 1) * size);
				p1 = terrainData[i1] / 32f;
				p2 = terrainData[i1 + 1] / 32f;
				p3 = terrainData[i2] / 32f;
				p4 = terrainData[i2 + 1] / 32f;
			} catch (Exception e) {
				return null;
			}
		}
		if (p2 >= 0 && p3 >= 0) {
			Vector3f result = new Vector3f();
			if (p1 >= 0) {
				Triangle tringle1 = new Triangle(new Vector3f(xInt * 2, yInt * 2, p1), new Vector3f(xInt * 2, (yInt + 1) * 2, p2),
					new Vector3f((xInt + 1) * 2, yInt * 2, p3));
				if (ray.intersectWhere(tringle1, result))
					return result;
			}
			if (p4 >= 0) {
				Triangle tringle2 = new Triangle(new Vector3f((xInt + 1) * 2, (yInt + 1) * 2, p4), new Vector3f(xInt * 2, (yInt + 1) * 2, p2),
					new Vector3f((xInt + 1) * 2, yInt * 2, p3));
				if (ray.intersectWhere(tringle2, result))
					return result;
			}
		}
		return null;
	}

	public boolean canSee(float x, float y, float z, float targetX, float targetY, float targetZ, float limit, int instanceId) {
		targetZ += 1;
		z += 1;
		// Another fix can see in instances
		// if (getZ(targetX, targetY) > targetZ)
		// return false;

		float x2 = x - targetX;
		float y2 = y - targetY;
		float distance = (float) Math.sqrt(x2 * x2 + y2 * y2);
		if (distance > 80f)
			return false;
		int intD = (int) Math.abs(distance);

		Vector3f pos = new Vector3f(x, y, z);
		Vector3f dir = new Vector3f(targetX, targetY, targetZ);
		dir.subtractLocal(pos).normalizeLocal();
		Ray r = new Ray(pos, dir);
		r.setLimit(limit);
		for (float s = 2; s < intD; s += 2) {
			float tempX = targetX + (x2 * s / distance);
			float tempY = targetY + (y2 * s / distance);
			Vector3f result = terrainCollision(tempX, tempY, r);
			if (result != null)
				return false;
		}
		CollisionResults results = new CollisionResults((byte) (CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId()), false,
			instanceId);
		int collisions = this.collideWith(r, results);
		return (results.size() == 0 && collisions == 0);
	}

	@Override
	public void updateModelBound() {
		if (getChildren() != null) {
			Iterator<Spatial> i = getChildren().iterator();
			while (i.hasNext()) {
				Spatial s = i.next();
				if (s instanceof Node && ((Node) s).getChildren().isEmpty()) {
					i.remove();
				}
			}
		}
		super.updateModelBound();
	}
}
