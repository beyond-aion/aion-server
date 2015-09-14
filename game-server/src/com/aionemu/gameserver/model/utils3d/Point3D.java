package com.aionemu.gameserver.model.utils3d;

/**
 * @author M@xx modified by Wakizashi
 */
public class Point3D {

	public double x;
	public double y;
	public double z;

	public Point3D() {
		this.x = 0.0;
		this.y = 0.0;
		this.z = 0.0;
	}

	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double distance(Point3D p) {
		double dx = x - p.x;
		double dy = y - p.y;
		double dz = z - p.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + y + ", z=" + z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
}
