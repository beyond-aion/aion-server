package com.aionemu.gameserver.world;

/**
 * Position of object in the world.
 * 
 * @author -Nemesiss-
 */
public class WorldPosition {

	private final int mapId;
	private MapRegion mapRegion;
	private float x, y, z;

	/**
	 * Value from 0 to 120 (120==0 actually)
	 */
	private byte heading;

	public WorldPosition(int mapId) {
		this.mapId = mapId;
	}

	public WorldPosition(int mapId, float x, float y, float z, byte h) {
		this(mapId, x, y, z, h, null);
	}

	public WorldPosition(int mapId, float x, float y, float z, byte h, MapRegion mapRegion) {
		if (mapId <= 0)
			throw new IllegalArgumentException("mapId must be greater than 0");
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = h;
		this.mapRegion = mapRegion;
	}

	public int getMapId() {
		return mapId;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

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
	 * @return The world id the client knows this map instance by: the map id plus the channel (instance id - 1), negated on personal maps.
	 */
	public int getClientWorldId() {
		int worldId = mapId + getInstanceId() - 1;
		WorldMapType worldMapType = WorldMapType.getWorld(mapId);
		return worldMapType != null && worldMapType.isPersonal() ? -worldId : worldId;
	}

	public boolean isInstanceMap() {
		return mapRegion != null && mapRegion.getParent().getParent().isInstanceType();
	}

	public byte getHeading() {
		return heading;
	}

	public WorldMapInstance getWorldMapInstance() {
		return mapRegion.getParent();
	}

	void setMapRegion(MapRegion r) {
		mapRegion = r;
	}

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
		if (!(obj instanceof WorldPosition other))
			return false;
		return this.mapId == other.mapId && this.x == other.x && this.y == other.y && this.z == other.z && this.heading == other.heading;
	}

	@Override
	public String toString() {
		return "WorldPosition [mapId=" + mapId + ", x=" + x + ", y=" + y + ", z=" + z + ", heading=" + heading + "]";
	}

	public String toCoordString() {
		return "Map ID: " + mapId + ", Instance ID: " + getInstanceId() + ", X: " + x + ", Y: " + y + ", Z: " + z + ", Heading: " + heading;
	}
}
