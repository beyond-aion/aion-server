package com.aionemu.gameserver.model.geometry;

import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author MrPoke
 */
public class SphereArea implements Area {

	protected final float x;
	protected final float y;
	protected final float z;
	protected final float r;

	public SphereArea(float x, float y, float z, float r) {
		if (r <= 0)
			throw new IllegalArgumentException("Radius must be greater than 0");
		this.x = x;
		this.y = y;
		this.z = z;
		this.r = r;
	}

	@Deprecated
	@Override
	public boolean isInside2D(Point2D point) {
		return false;
	}

	@Deprecated
	@Override
	public boolean isInside2D(float x, float y) {
		return false;
	}

	@Override
	public boolean isInside3D(Point3D point) {
		return PositionUtil.isInRange(x, y, z, point.getX(), point.getY(), point.getZ(), r);
	}

	@Override
	public boolean isInside3D(float x, float y, float z) {
		return PositionUtil.isInRange(x, y, z, this.x, this.y, this.z, r);
	}

	@Override
	public boolean isInsideZ(Point3D point) {
		return isInsideZ(point.getZ());
	}

	@Override
	public boolean isInsideZ(float z) {
		return z >= this.getMinZ() && z <= this.getMaxZ();
	}

	@Deprecated
	@Override
	public double getDistance2D(Point2D point) {
		return 0;
	}

	@Deprecated
	@Override
	public double getDistance2D(float x, float y) {
		return 0;
	}

	@Override
	public double getDistance3D(Point3D point) {
		return getDistance3D(point.getX(), point.getY(), point.getZ());
	}

	@Override
	public double getDistance3D(float x, float y, float z) {
		double distance = PositionUtil.getDistance(x, y, z, this.x, this.y, this.z) - r;
		return distance > 0 ? distance : 0;
	}

	@Deprecated
	@Override
	public Point2D getClosestPoint(Point2D point) {
		return null;
	}

	@Deprecated
	@Override
	public Point2D getClosestPoint(float x, float y) {
		return null;
	}

	@Override
	public Point3D getClosestPoint(Point3D point) {
		return null;
	}

	@Override
	public Point3D getClosestPoint(float x, float y, float z) {
		return null;
	}

	@Override
	public float getMinZ() {
		return z - r;
	}

	@Override
	public float getMaxZ() {
		return z + r;
	}

	@Override
	public boolean intersectsRectangle(RectangleArea area) {
		return area.getDistance3D(x, y, z) <= r;
	}
}
