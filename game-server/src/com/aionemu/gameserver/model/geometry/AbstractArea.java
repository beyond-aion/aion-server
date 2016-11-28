package com.aionemu.gameserver.model.geometry;

import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Class with basic method implementation for ares.<br>
 * If possible it should be subclassed. <br>
 * In other case {@link com.aionemu.gameserver.model.geometry.Area} should be implemented directly
 */
public abstract class AbstractArea implements Area {

	/**
	 * Minimal z of area
	 */
	private final float minZ;

	/**
	 * Maximal Z of area
	 */
	private final float maxZ;

	private ZoneName zoneName;

	private int worldId;

	/**
	 * Creates new AbstractArea with min and max z
	 * 
	 * @param minZ
	 *          min z
	 * @param maxZ
	 *          max z
	 */
	protected AbstractArea(ZoneName zoneName, int worldId, float minZ, float maxZ) {
		if (minZ > maxZ) {
			throw new IllegalArgumentException("minZ(" + minZ + ") > maxZ(" + maxZ + ")");
		}
		this.minZ = minZ;
		this.maxZ = maxZ;
		this.zoneName = zoneName;
		this.worldId = worldId;
	}

	@Override
	public boolean isInside2D(Point2D point) {
		return isInside2D(point.getX(), point.getY());
	}

	@Override
	public boolean isInside3D(Point3D point) {
		return isInside3D(point.getX(), point.getY(), point.getZ());
	}

	@Override
	public boolean isInside3D(float x, float y, float z) {
		return isInsideZ(z) && isInside2D(x, y);
	}

	@Override
	public boolean isInsideZ(Point3D point) {
		return isInsideZ(point.getZ());
	}

	@Override
	public boolean isInsideZ(float z) {
		return z >= getMinZ() && z <= getMaxZ();
	}

	@Override
	public double getDistance2D(Point2D point) {
		return getDistance2D(point.getX(), point.getY());
	}

	@Override
	public double getDistance3D(Point3D point) {
		return getDistance3D(point.getX(), point.getY(), point.getZ());
	}

	@Override
	public Point2D getClosestPoint(Point2D point) {
		return getClosestPoint(point.getX(), point.getY());
	}

	@Override
	public Point3D getClosestPoint(Point3D point) {
		return getClosestPoint(point.getX(), point.getY(), point.getZ());
	}

	@Override
	public Point3D getClosestPoint(float x, float y, float z) {
		Point2D closest2d = getClosestPoint(x, y);

		float zCoord;

		if (isInsideZ(z)) {
			zCoord = z;
		} else if (z < getMinZ()) {
			zCoord = getMinZ();
		} else {
			zCoord = getMaxZ();
		}

		return new Point3D(closest2d.getX(), closest2d.getY(), zCoord);
	}

	@Override
	public float getMinZ() {
		return minZ;
	}

	@Override
	public float getMaxZ() {
		return maxZ;
	}

	@Override
	public int getWorldId() {
		return worldId;
	}

	/**
	 * @return the zoneName
	 */
	@Override
	public ZoneName getZoneName() {
		return zoneName;
	}
}
