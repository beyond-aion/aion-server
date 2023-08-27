package com.aionemu.gameserver.model.geometry;

import com.aionemu.gameserver.geoEngine.math.Vector3f;

/**
 * @author Neon
 */
public class Plane3D {

	private final Vector3f pointOnPlane;
	private final Vector3f normal;

	public Plane3D(Vector3f p1, Vector3f p2, Vector3f p3) {
		this.pointOnPlane = p1;
		Vector3f vector1 = p2.subtract(p1);
		Vector3f vector2 = p3.subtract(p1);
		normal = vector1.cross(vector2);
	}

	public Vector3f intersection(Vector3f rayStart, Vector3f rayEnd) {
		Vector3f rayDirection = rayEnd.subtract(rayStart);
		float dotProduct = normal.dot(rayDirection);
		if (dotProduct == 0) // ray is parallel to the plane
			return null;
		float distance = normal.dot(pointOnPlane.subtract(rayStart)) / dotProduct;
		if (distance < 0 || distance > 1) // intersection point is outside the range of the ray
			return null;
		return rayStart.add(rayDirection.multLocal(distance));
	}
}