package com.aionemu.gameserver.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;

/**
 * Position of object in the world.
 * 
 * @author -Nemesiss-
 */
public class WorldPosition {

	private static final Logger log = LoggerFactory.getLogger(WorldPosition.class);

	/**
	 * Map id.
	 */
	private final int mapId;

	/**
	 * Map Region.
	 */
	private MapRegion mapRegion;

	/**
	 * World position coordinate
	 */
	private float x, y, z;

	/**
	 * Value from 0 to 120 (120==0 actually)
	 */
	private byte heading;

	/**
	 * indicating if object is spawned or not.
	 */
	private volatile boolean isSpawned = false;

	public WorldPosition(int mapId) {
		this.mapId = mapId;
	}

	public WorldPosition(int mapId, float x, float y, float z, byte h) {
		this(mapId, x, y, z, h, null);
	}

	public WorldPosition(int mapId, float x, float y, float z, byte h, MapRegion mapRegion) {
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = h;
		this.mapRegion = mapRegion;
	}

	/**
	 * Return World map id.
	 * 
	 * @return world map id
	 */
	public int getMapId() {
		if (mapId == 0)
			log.warn("WorldPosition has (mapId == 0) " + this.toString());
		return mapId;
	}

	/**
	 * Return World position x
	 * 
	 * @return x
	 */
	public float getX() {
		return x;
	}

	/**
	 * Return World position y
	 * 
	 * @return y
	 */
	public float getY() {
		return y;
	}

	/**
	 * Return World position z
	 * 
	 * @return z
	 */
	public float getZ() {
		return z;
	}

	/**
	 * If you need the map region under any conditions use this method, but for most use cases you should use {@link VisibleObject#getMapRegion()},
	 * since it respects the isSpawned state.
	 * 
	 * @return Map region
	 */
	public MapRegion getMapRegion() {
		return mapRegion;
	}

	public boolean isMapRegionActive() {
		return mapRegion == null ? false : mapRegion.isActive();
	}

	public int getInstanceId() {
		return mapRegion == null ? 1 : mapRegion.getParent().getInstanceId();
	}

	/**
	 * @return
	 */
	public boolean isInstanceMap() {
		return mapRegion == null ? false : mapRegion.getParent().getParent().isInstanceType();
	}

	/**
	 * Return heading.
	 * 
	 * @return heading
	 */
	public byte getHeading() {
		return heading;
	}

	/**
	 * @return worldMapInstance
	 */
	public WorldMapInstance getWorldMapInstance() {
		return mapRegion.getParent();
	}

	/**
	 * Check if object is spawned.
	 * 
	 * @return true if object is spawned.
	 */
	public boolean isSpawned() {
		return isSpawned;
	}

	/**
	 * Set isSpawned to given value.
	 * 
	 * @param val
	 */
	void setIsSpawned(boolean val) {
		isSpawned = val;
	}

	/**
	 * Set map region
	 * 
	 * @param r
	 *          - map region
	 */
	void setMapRegion(MapRegion r) {
		mapRegion = r;
	}

	/**
	 * Set world position.
	 * 
	 * @param newX
	 * @param newY
	 * @param newZ
	 * @param newHeading
	 *          Value from 0 to 120 (120==0 actually)
	 */
	public void setXYZH(Float newX, Float newY, Float newZ, Byte newHeading) {
		if (newX != null)
			x = newX;
		if (newY != null)
			y = newY;
		if (newZ != null)
			z = newZ;
		if (newHeading != null)
			heading = newHeading;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public void setH(byte h) {
		this.heading = h;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + heading;
		result = prime * result + mapId;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof WorldPosition))
			return false;
		WorldPosition other = (WorldPosition) obj;
		return this.mapId == other.mapId && this.x == other.x && this.y == other.y && this.z == other.z && this.heading == other.heading;
	}

	@Override
	public String toString() {
		return "WorldPosition [mapId=" + mapId + ", x=" + x + ", y=" + y + ", z=" + z + ", heading=" + heading + ", isSpawned=" + isSpawned + "]";
	}

	public String toCoordString() {
		return "Map ID: " + mapId + ", Instance ID: " + getInstanceId() + "\nX: " + x + ", Y: " + y + ", Z: " + z + ", Heading: " + heading;
	}
}
