package com.aionemu.gameserver.model.geometry;

import java.util.Collection;

import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Area of free form
 * 
 * @author SoulKeeper
 */
public class PolyArea extends AbstractArea {

	/**
	 * Polygon used to calculate isInside()
	 */
	private final Polygon2D poly;

	/**
	 * Creates new area from given points
	 * 
	 * @param points
	 *          list of points
	 * @param zMin
	 *          minimal z
	 * @param zMax
	 *          maximal z
	 */
	public PolyArea(ZoneName zoneName, int worldId, Collection<Point2D> points, float zMin, float zMax) {
		this(zoneName, worldId, points.toArray(new Point2D[points.size()]), zMin, zMax);
	}

	/**
	 * Creates new area from given points
	 * 
	 * @param points
	 *          list of points
	 * @param zMin
	 *          minimal z
	 * @param zMax
	 *          maximal z
	 */
	public PolyArea(ZoneName zoneName, int worldId, Point2D[] points, float zMin, float zMax) {
		super(zoneName, worldId, zMin, zMax);

		if (points.length < 3) {
			throw new IllegalArgumentException("Not enough points, needed at least 3 but got " + points.length);
		}

		float[] xPoints = new float[points.length];
		float[] yPoints = new float[points.length];

		for (int i = 0, n = points.length; i < n; i++) {
			Point2D p = points[i];
			xPoints[i] = p.getX();
			yPoints[i] = p.getY();
		}
		this.poly = new Polygon2D(xPoints, yPoints, points.length);
	}

	@Override
	public boolean isInside2D(float x, float y) {
		return poly.contains(x, y);
	}

	@Override
	public double getDistance2D(float x, float y) {
		if (isInside2D(x, y)) {
			return 0;
		} else {
			Point2D cp = getClosestPoint(x, y);
			return PositionUtil.getDistance(cp.getX(), cp.getY(), x, y);
		}
	}

	@Override
	public double getDistance3D(float x, float y, float z) {
		if (isInside3D(x, y, z)) {
			return 0;
		} else if (isInsideZ(z)) {
			return getDistance2D(x, y);
		} else {
			Point3D cp = getClosestPoint(x, y, z);
			return PositionUtil.getDistance(cp.getX(), cp.getY(), cp.getZ(), x, y, z);
		}
	}

	@Override
	public Point2D getClosestPoint(float x, float y) {

		Point2D closestPoint = null;
		double closestDistance = 0;
		for (int i = 0; i < poly.xpoints.length; i++) {
			int nextIndex = i + 1;
			if (nextIndex == poly.xpoints.length) {
				nextIndex = 0;
			}

			float p1x = poly.xpoints[i];
			float p1y = poly.ypoints[i];
			float p2x = poly.xpoints[nextIndex];
			float p2y = poly.ypoints[nextIndex];

			Point2D point = PositionUtil.getClosestPointOnSegment(p1x, p1y, p2x, p2y, x, y);

			if (closestPoint == null) {
				closestPoint = point;
				closestDistance = PositionUtil.getDistance(closestPoint.getX(), closestPoint.getY(), x, y);
			} else {
				double newDistance = PositionUtil.getDistance(point.getX(), point.getY(), x, y);
				if (newDistance < closestDistance) {
					closestPoint = point;
					closestDistance = newDistance;
				}
			}
		}

		return closestPoint;
	}

	@Override
	public boolean intersectsRectangle(RectangleArea area) {
		if (area.getMinZ() > getMaxZ() || area.getMaxZ() < getMinZ())
			return false;
		return poly.intersects(area.getMinX(), area.getMinY(), WorldConfig.WORLD_REGION_SIZE, WorldConfig.WORLD_REGION_SIZE);
	}
}
