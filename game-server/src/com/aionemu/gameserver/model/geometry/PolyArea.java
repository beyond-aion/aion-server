package com.aionemu.gameserver.model.geometry;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * Area of free form
 * 
 * @author SoulKeeper
 */
public class PolyArea extends AbstractArea {

	private final List<Point2D> points;
	private final Rectangle2D bounds;
	private final Path2D.Float path;

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
	public PolyArea(List<Point2D> points, float zMin, float zMax) {
		if (points.size() < 3)
			throw new IllegalArgumentException("PolyArea needs at least 3 points but got " + points.size());
		super(zMin, zMax);

		this.points = points;
		path = new Path2D.Float(Path2D.WIND_NON_ZERO, points.size());
		path.moveTo(points.getFirst().getX(), points.getFirst().getY());
		for (int i = 1; i < points.size(); i++) {
			path.lineTo(points.get(i).getX(), points.get(i).getY());
		}
		bounds = path.getBounds2D();
		path.closePath();
	}

	@Override
	public boolean isInside2D(float x, float y) {
		return bounds.contains(x, y) && path.contains(x, y);
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
		for (int i = 0; i < points.size(); i++) {
			int nextIndex = i + 1;
			if (nextIndex == points.size()) {
				nextIndex = 0;
			}
			float p1x = points.get(i).getX();
			float p1y = points.get(i).getY();
			float p2x = points.get(nextIndex).getX();
			float p2y = points.get(nextIndex).getY();
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
		return intersects(area.getMinX(), area.getMinY(), WorldConfig.WORLD_REGION_SIZE, WorldConfig.WORLD_REGION_SIZE);
	}

	private boolean intersects(double x, double y, double w, double h) {
		return bounds.intersects(x, y, w, h) && path.intersects(x, y, w, h);
	}
}
