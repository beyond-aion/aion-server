package com.aionemu.gameserver.model.geometry;

import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Rolandas
 */
public class SemisphereArea extends SphereArea {

	public SemisphereArea(ZoneName zoneName, int worldId, float x, float y, float z, float r) {
		super(zoneName, worldId, x, y, z, r);
	}

	@Override
	public boolean isInside3D(Point3D point) {
		return this.z < point.getZ() && PositionUtil.isInRange(x, y, z, point.getX(), point.getY(), point.getZ(), r);
	}

	@Override
	public boolean isInside3D(float x, float y, float z) {
		return this.z < z && PositionUtil.isInRange(x, y, z, this.x, this.y, this.z, r);
	}

	@Override
	public boolean isInsideZ(Point3D point) {
		return isInsideZ(point.getZ());
	}

	@Override
	public float getMinZ() {
		return z;
	}

	@Override
	public float getMaxZ() {
		return z + r;
	}

	@Override
	public double getDistance3D(Point3D point) {
		return getDistance3D(point.getX(), point.getY(), point.getZ());
	}

	@Override
	public double getDistance3D(float x, float y, float z) {
		double distance = PositionUtil.getDistance(x, y, z, this.x, this.y, this.z) - r;
		if (z < this.z)
			return distance;
		return distance > 0 ? distance : 0;
	}

	@Override
	public boolean intersectsRectangle(RectangleArea area) {
		if ((area.getMaxZ() >= z || z <= area.getMinZ()) && area.getDistance3D(x, y, z) <= r)
			return true;
		return false;
	}

}
