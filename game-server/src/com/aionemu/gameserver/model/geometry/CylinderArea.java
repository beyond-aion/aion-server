package com.aionemu.gameserver.model.geometry;

import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * This class implements cylinder area
 * 
 * @author SoulKeeper
 */
public class CylinderArea extends AbstractArea {

	/**
	 * Center of cylinder
	 */
	private final float centerX;

	/**
	 * Center of cylinder
	 */
	private final float centerY;

	/**
	 * Cylinder radius
	 */
	private final float radius;

	/**
	 * Creates new cylinder with given radius
	 * 
	 * @param center
	 *          center of the circle
	 * @param radius
	 *          radius of the circle
	 * @param minZ
	 *          min z
	 * @param maxZ
	 *          max z
	 */
	public CylinderArea(ZoneName zoneName, int worldId, Point2D center, float radius, float minZ, float maxZ) {
		this(zoneName, worldId, center.getX(), center.getY(), radius, minZ, maxZ);
	}

	/**
	 * Creates new cylider with given radius
	 * 
	 * @param x
	 *          center coord
	 * @param y
	 *          center coord
	 * @param radius
	 *          radius of the circle
	 * @param minZ
	 *          min z
	 * @param maxZ
	 *          max z
	 */
	public CylinderArea(ZoneName zoneName, int worldId, float x, float y, float radius, float minZ, float maxZ) {
		super(zoneName, worldId, minZ, maxZ);
		this.centerX = x;
		this.centerY = y;
		this.radius = radius;
	}

	@Override
	public boolean isInside2D(float x, float y) {
		return PositionUtil.getDistance(centerX, centerY, x, y) < radius;
	}

	@Override
	public double getDistance2D(float x, float y) {
		if (isInside2D(x, y)) {
			return 0;
		} else {
			return Math.abs(PositionUtil.getDistance(centerX, centerY, x, y) - radius);
		}
	}

	@Override
	public double getDistance3D(float x, float y, float z) {
		if (isInside3D(x, y, z)) {
			return 0;
		} else if (isInsideZ(z)) {
			return getDistance2D(x, y);
		} else {
			if (z < getMinZ()) {
				return PositionUtil.getDistance(centerX, centerY, getMinZ(), x, y, z);
			} else {
				return PositionUtil.getDistance(centerX, centerY, getMaxZ(), x, y, z);
			}
		}
	}

	@Override
	public Point2D getClosestPoint(float x, float y) {
		if (isInside2D(x, y)) {
			return new Point2D(x, y);
		} else {
			float vX = x - this.centerX;
			float vY = y - this.centerY;
			double magV = PositionUtil.getDistance(centerX, centerY, x, y);
			double pointX = centerX + vX / magV * radius;
			double pointY = centerY + vY / magV * radius;
			return new Point2D((float) pointX, (float) pointY);
		}
	}

	@Override
	public boolean intersectsRectangle(RectangleArea area) {
		if (area.getMinZ() > getMaxZ() || area.getMaxZ() < getMinZ())
			return false;
		if (area.getDistance2D(centerX, centerY) < radius)
			return true;
		return false;
	}
}
