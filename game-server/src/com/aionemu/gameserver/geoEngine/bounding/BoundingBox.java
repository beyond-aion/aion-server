/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.aionemu.gameserver.geoEngine.bounding;

import java.nio.FloatBuffer;

import com.aionemu.gameserver.geoEngine.collision.*;
import com.aionemu.gameserver.geoEngine.math.*;
import com.aionemu.gameserver.geoEngine.utils.TempVars;

/**
 * <code>BoundingBox</code> defines an axis-aligned cube that defines a container for a group of vertices of a
 * particular piece of geometry. This box defines a center and extents from that center along the x, y and z axis. <br>
 * <br>
 * A typical usage is to allow the class define the center and radius by calling either <code>containAABB</code> or
 * <code>averagePoints</code>. A call to <code>computeFramePoint</code> in turn calls <code>containAABB</code>.
 * 
 * @author Joshua Slack
 * @version $Id: BoundingBox.java,v 1.50 2007/09/22 16:46:35 irrisor Exp $
 */
public class BoundingBox extends BoundingVolume {

	float xExtent, yExtent, zExtent;

	/**
	 * Default constructor instantiates a new <code>BoundingBox</code> object.
	 */
	public BoundingBox() {
	}

	/**
	 * Contstructor instantiates a new <code>BoundingBox</code> object with given specs.
	 */
	public BoundingBox(Vector3f c, float x, float y, float z) {
		this.center.set(c);
		this.xExtent = x;
		this.yExtent = y;
		this.zExtent = z;
	}

	public BoundingBox(BoundingBox source) {
		this.center.set(source.center);
		this.xExtent = source.xExtent;
		this.yExtent = source.yExtent;
		this.zExtent = source.zExtent;
	}

	public BoundingBox(Vector3f min, Vector3f max) {
		setMinMax(min, max);
	}

	@Override
	public Type getType() {
		return Type.AABB;
	}

	/**
	 * <code>computeFromPoints</code> creates a new Bounding Box from a given set of points. It uses the
	 * <code>containAABB</code> method as default.
	 * 
	 * @param points
	 *          the points to contain.
	 */
	@Override
	public void computeFromPoints(FloatBuffer points) {
		containAABB(points);
	}

	public static void checkMinMax(Vector3f min, Vector3f max, Vector3f point) {
		if (point.x < min.x)
			min.x = point.x;
		if (point.x > max.x)
			max.x = point.x;
		if (point.y < min.y)
			min.y = point.y;
		if (point.y > max.y)
			max.y = point.y;
		if (point.z < min.z)
			min.z = point.z;
		if (point.z > max.z)
			max.z = point.z;
	}

	/**
	 * <code>containAABB</code> creates a minimum-volume axis-aligned bounding box of the points, then selects the
	 * smallest enclosing sphere of the box with the sphere centered at the boxes center.
	 * 
	 * @param points
	 *          the list of points.
	 */
	public void containAABB(FloatBuffer points) {
		if (points == null)
			return;

		if (points.limit() <= 2) // we need at least a 3 float vector
			throw new IllegalArgumentException();

		float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

		TempVars vars = TempVars.get();
		Vector3f vect1 = vars.vect1;
		for (int i = 0; i < points.limit();) {
			vect1.x = points.get(i++);
			vect1.y = points.get(i++);
			vect1.z = points.get(i++);
			if (vect1.x < minX)
				minX = vect1.x;
			if (vect1.x > maxX)
				maxX = vect1.x;

			if (vect1.y < minY)
				minY = vect1.y;
			if (vect1.y > maxY)
				maxY = vect1.y;

			if (vect1.z < minZ)
				minZ = vect1.z;
			if (vect1.z > maxZ)
				maxZ = vect1.z;
		}
		vars.release();

		center.set(minX + maxX, minY + maxY, minZ + maxZ);
		center.multLocal(0.5f);

		xExtent = maxX - center.x;
		yExtent = maxY - center.y;
		zExtent = maxZ - center.z;
	}

	@Override
	public BoundingVolume transform(Matrix4f trans, BoundingVolume store) {
		BoundingBox box;
		if (store == null || store.getType() != Type.AABB) {
			box = new BoundingBox();
		} else {
			box = (BoundingBox) store;
		}

		float w = trans.multProj(center, box.center);
		box.center.divideLocal(w);

		TempVars vars = TempVars.get();
		Matrix3f transMatrix = vars.tempMat3;
		trans.toRotationMatrix(transMatrix);

		// Make the rotation matrix all positive to get the maximum x/y/z extent
		transMatrix.absoluteLocal();

		vars.vect1.set(xExtent, yExtent, zExtent);
		transMatrix.mult(vars.vect1, vars.vect1);

		// Assign the biggest rotations after scales.
		box.xExtent = FastMath.abs(vars.vect1.getX());
		box.yExtent = FastMath.abs(vars.vect1.getY());
		box.zExtent = FastMath.abs(vars.vect1.getZ());

		vars.release();
		return box;
	}

	/**
	 * <code>mergeLocal</code> combines this sphere with a second bounding sphere locally. Altering this sphere to contain
	 * both the original and the additional sphere volumes;
	 * 
	 * @param volume
	 *          the sphere to combine with this sphere.
	 * @return this
	 */
	@Override
	public BoundingVolume mergeLocal(BoundingVolume volume) {
		if (volume == null) {
			return this;
		}

		switch (volume.getType()) {
			case AABB: {
				BoundingBox vBox = (BoundingBox) volume;
				return mergeLocal(vBox.center, vBox.xExtent, vBox.yExtent, vBox.zExtent);
			}

			// case OBB: {
			// return mergeOBB((OrientedBoundingBox) volume);
			// }

			default:
				return null;
		}
	}

	/**
	 * <code>mergeLocal</code> combines this bounding box locally with a second
	 * bounding box described by its center and extents.
	 *
	 * @param boxCenter the center of the second box (not null, not altered)
	 * @param boxX the X-extent of the second box
	 * @param boxY the Y-extent of the second box
	 * @param boxZ the Z-extent of the second box
	 * @return this
	 */
	private BoundingBox mergeLocal(Vector3f boxCenter, float boxX, float boxY, float boxZ) {
		if (xExtent == Float.POSITIVE_INFINITY || boxX == Float.POSITIVE_INFINITY) {
			center.x = 0;
			xExtent = Float.POSITIVE_INFINITY;
		} else {
			float low = center.x - xExtent;
			if (low > boxCenter.x - boxX) {
				low = boxCenter.x - boxX;
			}
			float high = center.x + xExtent;
			if (high < boxCenter.x + boxX) {
				high = boxCenter.x + boxX;
			}
			center.x = (low + high) / 2;
			xExtent = high - center.x;
		}

		if (yExtent == Float.POSITIVE_INFINITY || boxY == Float.POSITIVE_INFINITY) {
			center.y = 0;
			yExtent = Float.POSITIVE_INFINITY;
		} else {
			float low = center.y - yExtent;
			if (low > boxCenter.y - boxY) {
				low = boxCenter.y - boxY;
			}
			float high = center.y + yExtent;
			if (high < boxCenter.y + boxY) {
				high = boxCenter.y + boxY;
			}
			center.y = (low + high) / 2;
			yExtent = high - center.y;
		}

		if (zExtent == Float.POSITIVE_INFINITY || boxZ == Float.POSITIVE_INFINITY) {
			center.z = 0;
			zExtent = Float.POSITIVE_INFINITY;
		} else {
			float low = center.z - zExtent;
			if (low > boxCenter.z - boxZ) {
				low = boxCenter.z - boxZ;
			}
			float high = center.z + zExtent;
			if (high < boxCenter.z + boxZ) {
				high = boxCenter.z + boxZ;
			}
			center.z = (low + high) / 2;
			zExtent = high - center.z;
		}

		return this;
	}

	/**
	 * <code>clone</code> creates a new BoundingBox object containing the same data as this one.
	 * 
	 * @param store
	 *          where to store the cloned information. if null or wrong class, a new store is created.
	 * @return the new BoundingBox
	 */
	@Override
	public BoundingBox clone(BoundingVolume store) {
		BoundingBox rVal;
		if (store != null && store.getType() == Type.AABB)
			rVal = (BoundingBox) store;
		else
			rVal = new BoundingBox();
		rVal.center.set(center);
		rVal.xExtent = xExtent;
		rVal.yExtent = yExtent;
		rVal.zExtent = zExtent;
		rVal.isTreeCollidable = isTreeCollidable;
		return rVal;
	}

	/**
	 * <code>toString</code> returns the string representation of this object. The form is:
	 * "Radius: RRR.SSSS Center: <Vector>".
	 * 
	 * @return the string representation of this.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [Center: " + center + "  xExtent: " + xExtent + "  yExtent: " + yExtent + "  zExtent: " + zExtent + "]";
	}

	/**
	 * determines if this bounding box intersects a given bounding sphere.
	 * 
	 * @see com.jme.bounding.BoundingVolume#intersectsSphere(com.jme.bounding.BoundingSphere)
	 */
	@Override
	public boolean intersectsSphere(BoundingSphere bs) {
		return ((FastMath.abs(center.x - bs.center.x) < bs.getRadius() + xExtent) && (FastMath.abs(center.y - bs.center.y) < bs.getRadius() + yExtent)
			&& (FastMath.abs(center.z - bs.center.z) < bs.getRadius() + zExtent));
	}

	/**
	 * intersects determines if this Bounding Box intersects with another given bounding volume. If so, true is returned,
	 * otherwise, false is returned.
	 * 
	 * @see com.aionemu.gameserver.geoEngine.bounding.jme.bounding.BoundingVolume#intersects(com.aionemu.gameserver.geoEngine.bounding.jme.bounding.BoundingVolume)
	 */
	@Override
	public boolean intersects(BoundingVolume bv) {
		return bv.intersectsBoundingBox(this);
	}

	/**
	 * determines if this bounding box intersects a given bounding box. If the two boxes intersect in any way, true is
	 * returned. Otherwise, false is returned.
	 * 
	 * @see com.aionemu.gameserver.geoEngine.bounding.jme.bounding.BoundingVolume#intersectsBoundingBox(com.aionemu.gameserver.geoEngine.bounding.jme.bounding.BoundingBox)
	 */
	@Override
	public boolean intersectsBoundingBox(BoundingBox bb) {
		assert Vector3f.isValidVector(center) && Vector3f.isValidVector(bb.center);

		if (center.x + xExtent < bb.center.x - bb.xExtent || center.x - xExtent > bb.center.x + bb.xExtent)
			return false;
		else if (center.y + yExtent < bb.center.y - bb.yExtent || center.y - yExtent > bb.center.y + bb.yExtent)
			return false;
		else if (center.z + zExtent < bb.center.z - bb.zExtent || center.z - zExtent > bb.center.z + bb.zExtent)
			return false;
		else
			return true;
	}

	/**
	 * determines if this bounding box intersects with a given ray object. If an intersection has occurred, true is
	 * returned, otherwise false is returned.
	 * 
	 * @see com.aionemu.gameserver.geoEngine.bounding.BoundingVolume#intersects(Ray)
	 */
	@Override
	public boolean intersects(Ray ray) {
		TempVars vars = TempVars.get();
		Vector3f diff = ray.origin.subtract(getCenter(vars.vect2), vars.vect1);

		float[] fWdU = vars.fWdU;
		float[] fAWdU = vars.fAWdU;
		float[] fDdU = vars.fDdU;
		float[] fADdU = vars.fADdU;
		float[] fAWxDdU = vars.fAWxDdU;

		fWdU[0] = ray.getDirection().dot(Vector3f.UNIT_X);
		fAWdU[0] = FastMath.abs(fWdU[0]);
		fDdU[0] = diff.dot(Vector3f.UNIT_X);
		fADdU[0] = FastMath.abs(fDdU[0]);
		if (fADdU[0] > xExtent && fDdU[0] * fWdU[0] >= 0.0) {
			vars.release();
			return false;
		}

		fWdU[1] = ray.getDirection().dot(Vector3f.UNIT_Y);
		fAWdU[1] = FastMath.abs(fWdU[1]);
		fDdU[1] = diff.dot(Vector3f.UNIT_Y);
		fADdU[1] = FastMath.abs(fDdU[1]);
		if (fADdU[1] > yExtent && fDdU[1] * fWdU[1] >= 0.0) {
			vars.release();
			return false;
		}

		fWdU[2] = ray.getDirection().dot(Vector3f.UNIT_Z);
		fAWdU[2] = FastMath.abs(fWdU[2]);
		fDdU[2] = diff.dot(Vector3f.UNIT_Z);
		fADdU[2] = FastMath.abs(fDdU[2]);
		if (fADdU[2] > zExtent && fDdU[2] * fWdU[2] >= 0.0) {
			vars.release();
			return false;
		}

		Vector3f wCrossD = ray.getDirection().cross(diff, vars.vect2);

		fAWxDdU[0] = FastMath.abs(wCrossD.dot(Vector3f.UNIT_X));
		float rhs = yExtent * fAWdU[2] + zExtent * fAWdU[1];
		if (fAWxDdU[0] > rhs) {
			vars.release();
			return false;
		}

		fAWxDdU[1] = FastMath.abs(wCrossD.dot(Vector3f.UNIT_Y));
		rhs = xExtent * fAWdU[2] + zExtent * fAWdU[0];
		if (fAWxDdU[1] > rhs) {
			vars.release();
			return false;
		}

		fAWxDdU[2] = FastMath.abs(wCrossD.dot(Vector3f.UNIT_Z));
		rhs = xExtent * fAWdU[1] + yExtent * fAWdU[0];
		if (fAWxDdU[2] > rhs) {
			vars.release();
			return false;
		}

		vars.release();
		return true;
	}

	/**
	 * @see com.aionemu.gameserver.geoEngine.bounding.BoundingVolume#intersectsWhere(Ray)
	 */
	private int collideWithRay(Ray ray, CollisionResults results) {
		TempVars vars = TempVars.get();
		Vector3f diff = vars.vect1.set(ray.origin).subtractLocal(center);
		Vector3f direction = vars.vect2.set(ray.direction);

		float[] t = vars.fWdU; // use one of the TempVars arrays
		t[0] = 0;
		t[1] = ray.getLimit();
		int collisions = 0;

		float saveT0 = t[0], saveT1 = t[1];
		boolean notEntirelyClipped = clip(+direction.x, -diff.x - xExtent, t) && clip(-direction.x, +diff.x - xExtent, t)
			&& clip(+direction.y, -diff.y - yExtent, t) && clip(-direction.y, +diff.y - yExtent, t) && clip(+direction.z, -diff.z - zExtent, t)
			&& clip(-direction.z, +diff.z - zExtent, t);

		if (notEntirelyClipped && (t[0] != saveT0 || t[1] != saveT1)) {
			Vector3f contactPoint1 = new Vector3f(ray.direction).multLocal(t[0]).addLocal(ray.origin);
			results.addCollision(new CollisionResult(contactPoint1, t[0]));
			collisions++;
			if (t[1] > t[0]) {
				Vector3f contactPoint2 = new Vector3f(ray.direction).multLocal(t[1]).addLocal(ray.origin);
				results.addCollision(new CollisionResult(contactPoint2, t[1]));
				collisions++;
			}
		}
		if (results instanceof WorldBoundCollisionResults wbCollisionResults) {
			if (wbCollisionResults.shouldAddBoxCenterPlaneCollision()) {
				saveT0 = t[0];
				saveT1 = t[1];
				boolean rayIntersectsCenterPlane = clip(+direction.y, -diff.y, t) && clip(-direction.y, +diff.y, t) && (t[0] != saveT0 || t[1] != saveT1);
				if (rayIntersectsCenterPlane) {
					// This collision is of a "flat" box, meaning a rectangle with the width (xExtend * 2) and height (zExtent * 2) of this box.
					// Just imagine the original box lost one dimension, everything else stays the same.
					Vector3f centerPlaneContactPoint = new Vector3f(ray.direction).multLocal(t[0]).addLocal(ray.origin);
					wbCollisionResults.setBoxCenterPlaneCollision(new CollisionResult(centerPlaneContactPoint, t[0]));
				}
			}
		}
		vars.release();
		return collisions;
	}

	@Override
	public int collideWith(Collidable other, CollisionResults results) {
		if (other instanceof Ray ray) {
			return collideWithRay(ray, results);
		}
		throw new UnsupportedCollisionException("With: " + other.getClass().getSimpleName());
	}

	@Override
	public boolean contains(Vector3f point) {
		return FastMath.abs(center.x - point.x) < xExtent && FastMath.abs(center.y - point.y) < yExtent && FastMath.abs(center.z - point.z) < zExtent;
	}

	@Override
	public boolean intersects(Vector3f point) {
		return FastMath.abs(center.x - point.x) <= xExtent && FastMath.abs(center.y - point.y) <= yExtent && FastMath.abs(center.z - point.z) <= zExtent;
	}

	@Override
	public float distanceToEdge(Vector3f point) {
		// compute coordinates of point in box coordinate system
		TempVars vars = TempVars.get();
		Vector3f closest = point.subtract(center, vars.vect1);

		// project test point onto box
		float sqrDistance = 0.0f;
		float delta;

		if (closest.x < -xExtent) {
			delta = closest.x + xExtent;
			sqrDistance += delta * delta;
		} else if (closest.x > xExtent) {
			delta = closest.x - xExtent;
			sqrDistance += delta * delta;
		}

		if (closest.y < -yExtent) {
			delta = closest.y + yExtent;
			sqrDistance += delta * delta;
		} else if (closest.y > yExtent) {
			delta = closest.y - yExtent;
			sqrDistance += delta * delta;
		}

		if (closest.z < -zExtent) {
			delta = closest.z + zExtent;
			sqrDistance += delta * delta;
		} else if (closest.z > zExtent) {
			delta = closest.z - zExtent;
			sqrDistance += delta * delta;
		}

		vars.release();
		return FastMath.sqrt(sqrDistance);
	}

	/**
	 * <code>clip</code> determines if a line segment intersects the current test plane.
	 * 
	 * @param denom
	 *          the denominator of the line segment.
	 * @param numer
	 *          the numerator of the line segment.
	 * @param t
	 *          test values of the plane.
	 * @return true if the line segment intersects the plane, false otherwise.
	 */
	private boolean clip(float denom, float numer, float[] t) {
		// Return value is 'true' if line segment intersects the current test
		// plane. Otherwise 'false' is returned in which case the line segment
		// is entirely clipped.
		if (denom > 0.0f) {
			if (numer > denom * t[1])
				return false;
			if (numer > denom * t[0])
				t[0] = numer / denom;
			return true;
		} else if (denom < 0.0f) {
			if (numer > denom * t[0])
				return false;
			if (numer > denom * t[1])
				t[1] = numer / denom;
			return true;
		} else {
			return numer <= 0.0;
		}
	}

	/**
	 * Query extent.
	 * 
	 * @param store
	 *          where extent gets stored - null to return a new vector
	 * @return store / new vector
	 */
	public Vector3f getExtent(Vector3f store) {
		if (store == null) {
			store = new Vector3f();
		}
		store.set(xExtent, yExtent, zExtent);
		return store;
	}

	public float getXExtent() {
		return xExtent;
	}

	public float getYExtent() {
		return yExtent;
	}

	public float getZExtent() {
		return zExtent;
	}

	public void setXExtent(float xExtent) {
		if (xExtent < 0)
			throw new IllegalArgumentException();

		this.xExtent = xExtent;
	}

	public void setYExtent(float yExtent) {
		if (yExtent < 0)
			throw new IllegalArgumentException();

		this.yExtent = yExtent;
	}

	public void setZExtent(float zExtent) {
		if (zExtent < 0)
			throw new IllegalArgumentException();

		this.zExtent = zExtent;
	}

	public Vector3f getMin(Vector3f store) {
		if (store == null) {
			store = new Vector3f();
		}
		store.set(center).subtractLocal(xExtent, yExtent, zExtent);
		return store;
	}

	public Vector3f getMax(Vector3f store) {
		if (store == null) {
			store = new Vector3f();
		}
		store.set(center).addLocal(xExtent, yExtent, zExtent);
		return store;
	}

	public void setMinMax(Vector3f min, Vector3f max) {
		this.center.set(max).addLocal(min).multLocal(0.5f);
		xExtent = FastMath.abs(max.x - center.x);
		yExtent = FastMath.abs(max.y - center.y);
		zExtent = FastMath.abs(max.z - center.z);
	}

	@Override
	public float getVolume() {
		return (8 * xExtent * yExtent * zExtent);
	}
}
