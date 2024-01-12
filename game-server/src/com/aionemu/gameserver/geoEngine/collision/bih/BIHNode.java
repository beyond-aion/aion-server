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

package com.aionemu.gameserver.geoEngine.collision.bih;

import static java.lang.Math.*;

import java.util.ArrayList;

import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.FastMath;
import com.aionemu.gameserver.geoEngine.math.Matrix4f;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.utils.TempVars;

/**
 * Bounding Interval Hierarchy. Based on: Instant Ray Tracing: The Bounding Interval Hierarchy By Carsten Wächter and
 * Alexander Keller
 */
public final class BIHNode {

	private int leftIndex, rightIndex;

	private BIHNode left;
	private BIHNode right;
	private float leftPlane;
	private float rightPlane;
	private int axis;

	public BIHNode(int l, int r) {
		leftIndex = l;
		rightIndex = r;
		axis = 3; // indicates leaf
	}

	public BIHNode(int axis) {
		this.axis = axis;
	}

	public BIHNode() {
	}

	public BIHNode getLeftChild() {
		return left;
	}

	public void setLeftChild(BIHNode left) {
		this.left = left;
	}

	public float getLeftPlane() {
		return leftPlane;
	}

	public void setLeftPlane(float leftPlane) {
		this.leftPlane = leftPlane;
	}

	public BIHNode getRightChild() {
		return right;
	}

	public void setRightChild(BIHNode right) {
		this.right = right;
	}

	public float getRightPlane() {
		return rightPlane;
	}

	public void setRightPlane(float rightPlane) {
		this.rightPlane = rightPlane;
	}

	public static final class BIHStackData {

		private final BIHNode node;
		private final float min, max;

		BIHStackData(BIHNode node, float min, float max) {
			this.node = node;
			this.min = min;
			this.max = max;
		}

	}

	public final int intersectWhere(Ray r, Matrix4f worldMatrix, BIHTree tree, float sceneMin, float sceneMax, CollisionResults results) {
		TempVars vars = TempVars.get();
		ArrayList<BIHStackData> stack = vars.bihStack;
		stack.clear();

		Vector3f o = vars.vect1.set(r.getOrigin());
		Vector3f d = vars.vect2.set(r.getDirection());

		Matrix4f inv = worldMatrix.invert();

		inv.mult(r.getOrigin(), r.getOrigin());

		// Fixes rotation collision bug
		inv.multNormal(r.getDirection(), r.getDirection());
		// inv.multNormalAcross(r.getDirection(), r.getDirection());

		float[] origins = { r.getOrigin().x, r.getOrigin().y, r.getOrigin().z };

		float[] invDirections = { 1f / r.getDirection().x, 1f / r.getDirection().y, 1f / r.getDirection().z };

		r.getDirection().normalizeLocal();

		Vector3f v1 = vars.vect3, v2 = vars.vect4, v3 = vars.vect5;
		int cols = 0;

		stack.add(new BIHStackData(this, sceneMin, sceneMax));
		stackloop:
		while (stack.size() > 0) {

			BIHStackData data = stack.remove(stack.size() - 1);
			BIHNode node = data.node;
			float tMin = data.min, tMax = data.max;

			if (tMax < tMin)
				continue;

			while (node.axis != 3) { // while node is not a leaf
				int a = node.axis;

				// find the origin and direction value for the given axis
				float origin = origins[a];
				float invDirection = invDirections[a];

				float tNearSplit, tFarSplit;
				BIHNode nearNode, farNode;

				tNearSplit = (node.leftPlane - origin) * invDirection;
				tFarSplit = (node.rightPlane - origin) * invDirection;
				nearNode = node.left;
				farNode = node.right;

				if (invDirection < 0) {
					float tmpSplit = tNearSplit;
					tNearSplit = tFarSplit;
					tFarSplit = tmpSplit;

					BIHNode tmpNode = nearNode;
					nearNode = farNode;
					farNode = tmpNode;
				}

				if (tMin > tNearSplit && tMax < tFarSplit) {
					continue stackloop;
				}

				if (tMin > tNearSplit) {
					tMin = max(tMin, tFarSplit);
					node = farNode;
				} else if (tMax < tFarSplit) {
					tMax = min(tMax, tNearSplit);
					node = nearNode;
				} else {
					stack.add(new BIHStackData(farNode, max(tMin, tFarSplit), tMax));
					tMax = min(tMax, tNearSplit);
					node = nearNode;
				}
			}

			// a leaf
			for (int i = node.leftIndex; i <= node.rightIndex; i++) {
				tree.getTriangle(i, v1, v2, v3);

				float t = r.intersects(v1, v2, v3);
				if (!Float.isInfinite(t)) {
					worldMatrix.mult(v1, v1);
					worldMatrix.mult(v2, v2);
					worldMatrix.mult(v3, v3);
					float t_world = new Ray(o, d).intersects(v1, v2, v3);
					t = t_world;

					Vector3f tempVarsContactPoint = vars.vect6.set(d).multLocal(t).addLocal(o);
					float worldSpaceDist = o.distance(tempVarsContactPoint);
					// fix invisible walls
					if (worldSpaceDist > r.limit)
						continue;
					if (results.shouldInvalidateSlopingSurface()) {
						// taken from https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-rendering-a-triangle/geometry-of-a-triangle
						Vector3f planeNormal = v2.subtractLocal(v1).crossLocal(v3.subtractLocal(v1)).normalizeLocal();
						double elevationAngleRad = planeNormal.angleBetween(Vector3f.UNIT_Z);
						if (elevationAngleRad > FastMath.HALF_PI) // convert angle >90-180° to 0-90° range
							elevationAngleRad = Math.PI - elevationAngleRad;
						if (elevationAngleRad > results.getSlopingSurfaceAngleRad())
							tempVarsContactPoint.setZ(Float.NaN);
					}
					results.addCollision(new CollisionResult(new Vector3f(tempVarsContactPoint), worldSpaceDist));
					cols++;
					if (results.isOnlyFirst())
						break stackloop;
				}
			}
		}
		vars.release();
		r.setOrigin(o);
		r.setDirection(d);
		return cols;
	}
}
