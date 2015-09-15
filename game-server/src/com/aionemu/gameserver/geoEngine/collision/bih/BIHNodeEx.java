package com.aionemu.gameserver.geoEngine.collision.bih;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;

import com.aionemu.gameserver.geoEngine.collision.CollisionResultsEx;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Matrix4f;
import com.jme3.math.Ray;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;

/**
 * Modified replication of {@link com.jme3.collision.bih.BIHNode BIHNode} to cut down execution time.<br>
 * <small>Unfortunately that's a final class in the JME, so we couldn't extend it.</small>
 * 
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see com.jme3.collision.bih.BIHNode
 */
class BIHNodeEx {

	private int leftIndex, rightIndex;
	private BIHNodeEx left;
	private BIHNodeEx right;
	private float leftPlane;
	private float rightPlane;
	private int axis;

	protected BIHNodeEx(int l, int r) {
		leftIndex = l;
		rightIndex = r;
		axis = 3; // indicates leaf
	}

	protected BIHNodeEx(int axis) {
		this.axis = axis;
	}

	protected void setLeftChild(BIHNodeEx left) {
		this.left = left;
	}

	protected void setLeftPlane(float leftPlane) {
		this.leftPlane = leftPlane;
	}

	protected void setRightChild(BIHNodeEx right) {
		this.right = right;
	}

	protected void setRightPlane(float rightPlane) {
		this.rightPlane = rightPlane;
	}

	protected static final class BIHStackData {

		private final BIHNodeEx node;
		private final float min, max;

		BIHStackData(BIHNodeEx node, float min, float max) {
			this.node = node;
			this.min = min;
			this.max = max;
		}
	}

	protected final int intersectWhere(Collidable col, BoundingBox box, Matrix4f worldMatrix, BIHTreeEx tree, CollisionResults results) {

		BIHTempVars vars = BIHTempVars.get();
		List<BIHStackData> stack = vars.bihStack;
		stack.clear();

		float[] minExts = { box.getCenter().x - box.getXExtent(), box.getCenter().y - box.getYExtent(), box.getCenter().z - box.getZExtent() };

		float[] maxExts = { box.getCenter().x + box.getXExtent(), box.getCenter().y + box.getYExtent(), box.getCenter().z + box.getZExtent() };

		stack.add(new BIHStackData(this, 0, 0));

		Triangle t = new Triangle();
		int cols = 0;
		boolean returnFirstCollision = (results instanceof CollisionResultsEx && ((CollisionResultsEx) results).isOnlyFirst());

		stackloop: while (stack.size() > 0) {
			BIHNodeEx node = stack.remove(stack.size() - 1).node;

			while (node.axis != 3) {
				int a = node.axis;

				float maxExt = maxExts[a];
				float minExt = minExts[a];

				if (node.leftPlane < node.rightPlane) {
					// means there's a gap in the middle
					// if the box is in that gap, we stop there
					if (minExt > node.leftPlane && maxExt < node.rightPlane) {
						continue stackloop;
					}
				}

				if (maxExt < node.rightPlane) {
					node = node.left;
				} else if (minExt > node.leftPlane) {
					node = node.right;
				} else {
					stack.add(new BIHStackData(node.right, 0, 0));
					node = node.left;
				}
			}

			for (int i = node.leftIndex; i <= node.rightIndex; i++) {
				tree.getTriangle(i, t.get1(), t.get2(), t.get3());
				if (worldMatrix != null) {
					worldMatrix.mult(t.get1(), t.get1());
					worldMatrix.mult(t.get2(), t.get2());
					worldMatrix.mult(t.get3(), t.get3());
				}

				int added = col.collideWith(t, results);

				if (added > 0) {
					int index = tree.getTriangleIndex(i);
					int start = results.size() - added;

					for (int j = start; j < results.size(); j++) {
						CollisionResult cr = results.getCollisionDirect(j);
						cr.setTriangleIndex(index);
					}

					cols += added;
					if (returnFirstCollision)
						break stackloop;
				}
			}
		}
		vars.release();
		return cols;
	}

	protected final int intersectWhere(Ray r, Matrix4f worldMatrix, BIHTreeEx tree, float sceneMin, float sceneMax, CollisionResults results) {

		BIHTempVars vars = BIHTempVars.get();
		List<BIHStackData> stack = vars.bihStack;
		stack.clear();

		// float tHit = Float.POSITIVE_INFINITY;

		Vector3f o = vars.vect1.set(r.getOrigin());
		Vector3f d = vars.vect2.set(r.getDirection());

		Matrix4f inv = vars.tempMat4.set(worldMatrix).invertLocal();

		inv.mult(r.getOrigin(), r.getOrigin());

		// Fixes rotation collision bug
		inv.multNormal(r.getDirection(), r.getDirection());
		// inv.multNormalAcross(r.getDirection(), r.getDirection());

		float[] origins = { r.getOrigin().x, r.getOrigin().y, r.getOrigin().z };

		float[] invDirections = { 1f / r.getDirection().x, 1f / r.getDirection().y, 1f / r.getDirection().z };

		r.getDirection().normalizeLocal();

		Vector3f v1 = vars.vect3, v2 = vars.vect4, v3 = vars.vect5;
		int cols = 0;
		boolean returnFirstCollision = (results instanceof CollisionResultsEx && ((CollisionResultsEx) results).isOnlyFirst());

		stack.add(new BIHStackData(this, sceneMin, sceneMax));
		stackloop: while (stack.size() > 0) {

			BIHStackData data = stack.remove(stack.size() - 1);
			BIHNodeEx node = data.node;
			float tMin = data.min, tMax = data.max;

			if (tMax < tMin) {
				continue;
			}

			while (node.axis != 3) { // while node is not a leaf
				int a = node.axis;

				// find the origin and direction value for the given axis
				float origin = origins[a];
				float invDirection = invDirections[a];

				float tNearSplit, tFarSplit;
				BIHNodeEx nearNode, farNode;

				tNearSplit = (node.leftPlane - origin) * invDirection;
				tFarSplit = (node.rightPlane - origin) * invDirection;
				nearNode = node.left;
				farNode = node.right;

				if (invDirection < 0) {
					float tmpSplit = tNearSplit;
					tNearSplit = tFarSplit;
					tFarSplit = tmpSplit;

					BIHNodeEx tmpNode = nearNode;
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
					if (worldMatrix != null) {
						worldMatrix.mult(v1, v1);
						worldMatrix.mult(v2, v2);
						worldMatrix.mult(v3, v3);
						float t_world = new Ray(o, d).intersects(v1, v2, v3);
						t = t_world;
					}

					Vector3f contactNormal = Triangle.computeTriangleNormal(v1, v2, v3, null);
					Vector3f contactPoint = new Vector3f(d).multLocal(t).addLocal(o);
					float worldSpaceDist = o.distance(contactPoint);

					CollisionResult cr = new CollisionResult(contactPoint, worldSpaceDist);
					cr.setContactNormal(contactNormal);
					cr.setTriangleIndex(tree.getTriangleIndex(i));
					results.addCollision(cr);
					cols++;
					if (returnFirstCollision)
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
