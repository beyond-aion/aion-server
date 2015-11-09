package com.aionemu.gameserver.geoEngine.collision.bih;

import static java.lang.Math.max;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.aionemu.gameserver.geoEngine.collision.CollisionResultsEx;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.CollisionData;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.scene.mesh.VirtualIndexBuffer;
import com.jme3.scene.mesh.WrappedIndexBuffer;
import com.jme3.util.TempVars;

/**
 * Modified replication of {@linkplain com.jme3.collision.bih.BIHTree BIHTree}, to enter our custom <tt>intersectWhere</tt> methods.<br>
 * <small>Could be implemented as a subclass of BIHTree, which would only make sense if JME's {@linkplain com.jme3.collision.bih.BIHNode BIHNode} was
 * also extendible.</small>
 * 
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see com.jme3.collision.bih.BIHTree
 */
public class BIHTreeEx implements CollisionData {

	public static final int MAX_TREE_DEPTH = 100;
	public static final int MAX_TRIS_PER_NODE = 21;
	private BIHNodeEx rootEx;
	private int maxTrisPerNode;
	private int numTris;
	private float[] pointData;
	private int[] triIndices;

	private transient float[] bihSwapTmp;

	private void initTriList(FloatBuffer vb, IndexBuffer ib) {
		pointData = new float[numTris * 3 * 3];
		int p = 0;
		for (int i = 0; i < numTris * 3; i += 3) {
			int vert = ib.get(i) * 3;
			pointData[p++] = vb.get(vert++);
			pointData[p++] = vb.get(vert++);
			pointData[p++] = vb.get(vert);

			vert = ib.get(i + 1) * 3;
			pointData[p++] = vb.get(vert++);
			pointData[p++] = vb.get(vert++);
			pointData[p++] = vb.get(vert);

			vert = ib.get(i + 2) * 3;
			pointData[p++] = vb.get(vert++);
			pointData[p++] = vb.get(vert++);
			pointData[p++] = vb.get(vert);
		}

		triIndices = new int[numTris];
		for (int i = 0; i < numTris; i++) {
			triIndices[i] = i;
		}
	}

	public BIHTreeEx(Mesh mesh, int maxTrisPerNode) {
		this.maxTrisPerNode = maxTrisPerNode;

		if (maxTrisPerNode < 1 || mesh == null) {
			throw new IllegalArgumentException();
		}

		bihSwapTmp = new float[9];

		VertexBuffer vBuffer = mesh.getBuffer(Type.Position);
		if (vBuffer == null) {
			throw new IllegalArgumentException("A mesh should at least contain a Position buffer");
		}
		IndexBuffer ib = mesh.getIndexBuffer();
		FloatBuffer vb = (FloatBuffer) vBuffer.getData();

		if (ib == null) {
			ib = new VirtualIndexBuffer(mesh.getVertexCount(), mesh.getMode());
		} else if (mesh.getMode() != Mode.Triangles) {
			ib = new WrappedIndexBuffer(mesh);
		}

		numTris = ib.size() / 3;
		initTriList(vb, ib);
	}

	public BIHTreeEx(Mesh mesh) {
		this(mesh, MAX_TRIS_PER_NODE);
	}

	public void construct() {
		BoundingBox sceneBbox = createBox(0, numTris - 1);
		rootEx = createNode(0, numTris - 1, sceneBbox, 0);
	}

	private BoundingBox createBox(int l, int r) {
		TempVars vars = TempVars.get();

		Vector3f min = vars.vect1.set(new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
		Vector3f max = vars.vect2.set(new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));

		Vector3f v1 = vars.vect3, v2 = vars.vect4, v3 = vars.vect5;

		for (int i = l; i <= r; i++) {
			getTriangle(i, v1, v2, v3);
			BoundingBox.checkMinMax(min, max, v1);
			BoundingBox.checkMinMax(min, max, v2);
			BoundingBox.checkMinMax(min, max, v3);
		}

		BoundingBox bbox = new BoundingBox(min, max);
		vars.release();
		return bbox;
	}

	int getTriangleIndex(int triIndex) {
		return triIndices[triIndex];
	}

	private int sortTriangles(int l, int r, float split, int axis) {
		int pivot = l;
		int j = r;

		TempVars vars = TempVars.get();

		Vector3f v1 = vars.vect1, v2 = vars.vect2, v3 = vars.vect3;

		while (pivot <= j) {
			getTriangle(pivot, v1, v2, v3);
			v1.addLocal(v2).addLocal(v3).multLocal(FastMath.ONE_THIRD);
			if (v1.get(axis) > split) {
				swapTriangles(pivot, j);
				--j;
			} else {
				++pivot;
			}
		}

		vars.release();
		pivot = (pivot == l && j < pivot) ? j : pivot;
		return pivot;
	}

	private void setMinMax(BoundingBox bbox, boolean doMin, int axis, float value) {
		Vector3f min = bbox.getMin(null);
		Vector3f max = bbox.getMax(null);

		if (doMin) {
			min.set(axis, value);
		} else {
			max.set(axis, value);
		}

		bbox.setMinMax(min, max);
	}

	private float getMinMax(BoundingBox bbox, boolean doMin, int axis) {
		if (doMin) {
			return bbox.getMin(null).get(axis);
		} else {
			return bbox.getMax(null).get(axis);
		}
	}

	private BIHNodeEx createNode(int l, int r, BoundingBox nodeBbox, int depth) {
		if ((r - l) < maxTrisPerNode || depth > MAX_TREE_DEPTH) {
			return new BIHNodeEx(l, r);
		}

		BoundingBox currentBox = createBox(l, r);

		Vector3f exteriorExt = nodeBbox.getExtent(null);
		Vector3f interiorExt = currentBox.getExtent(null);
		exteriorExt.subtractLocal(interiorExt);

		int axis = 0;
		if (exteriorExt.x > exteriorExt.y) {
			if (exteriorExt.x > exteriorExt.z) {
				axis = 0;
			} else {
				axis = 2;
			}
		} else {
			if (exteriorExt.y > exteriorExt.z) {
				axis = 1;
			} else {
				axis = 2;
			}
		}
		if (exteriorExt.equals(Vector3f.ZERO)) {
			axis = 0;
		}

		// Arrays.sort(tris, l, r, comparators[axis]);
		float split = currentBox.getCenter().get(axis);
		int pivot = sortTriangles(l, r, split, axis);
		if (pivot == l || pivot == r) {
			pivot = (r + l) / 2;
		}

		// If one of the partitions is empty, continue with recursion: same level but different bbox
		if (pivot < l) {
			// Only right
			BoundingBox rbbox = new BoundingBox(currentBox);
			setMinMax(rbbox, true, axis, split);
			return createNode(l, r, rbbox, depth + 1);
		} else if (pivot > r) {
			// Only left
			BoundingBox lbbox = new BoundingBox(currentBox);
			setMinMax(lbbox, false, axis, split);
			return createNode(l, r, lbbox, depth + 1);
		} else {
			// Build the node
			BIHNodeEx node = new BIHNodeEx(axis);

			// Left child
			BoundingBox lbbox = new BoundingBox(currentBox);
			setMinMax(lbbox, false, axis, split);

			// The left node right border is the plane most right
			node.setLeftPlane(getMinMax(createBox(l, max(l, pivot - 1)), false, axis));
			node.setLeftChild(createNode(l, max(l, pivot - 1), lbbox, depth + 1)); // Recursive call

			// Right Child
			BoundingBox rbbox = new BoundingBox(currentBox);
			setMinMax(rbbox, true, axis, split);
			// The right node left border is the plane most left
			node.setRightPlane(getMinMax(createBox(pivot, r), true, axis));
			node.setRightChild(createNode(pivot, r, rbbox, depth + 1)); // Recursive call

			return node;
		}
	}

	public void getTriangle(int index, Vector3f v1, Vector3f v2, Vector3f v3) {
		int pointIndex = index * 9;

		v1.x = pointData[pointIndex++];
		v1.y = pointData[pointIndex++];
		v1.z = pointData[pointIndex++];

		v2.x = pointData[pointIndex++];
		v2.y = pointData[pointIndex++];
		v2.z = pointData[pointIndex++];

		v3.x = pointData[pointIndex++];
		v3.y = pointData[pointIndex++];
		v3.z = pointData[pointIndex++];
	}

	public void swapTriangles(int index1, int index2) {
		int p1 = index1 * 9;
		int p2 = index2 * 9;

		// store p1 in tmp
		System.arraycopy(pointData, p1, bihSwapTmp, 0, 9);

		// copy p2 to p1
		System.arraycopy(pointData, p2, pointData, p1, 9);

		// copy tmp to p2
		System.arraycopy(bihSwapTmp, 0, pointData, p2, 9);

		// swap indices
		int tmp2 = triIndices[index1];
		triIndices[index1] = triIndices[index2];
		triIndices[index2] = tmp2;
	}

	private int collideWithRay(Ray r, Matrix4f worldMatrix, BoundingVolume worldBound, CollisionResults results) {

		BIHTempVars vars = BIHTempVars.get();
		try {
			CollisionResultsEx boundResults = vars.collisionResults;
			boundResults.clear();
			if (results instanceof CollisionResultsEx)
				boundResults.copyAttributes((CollisionResultsEx) results);
			worldBound.collideWith(r, boundResults);
			if (boundResults.size() > 0) {
				float tMin = boundResults.getClosestCollision().getDistance();
				float tMax = boundResults.getFarthestCollision().getDistance();

				if (tMax <= 0) {
					tMax = Float.POSITIVE_INFINITY;
				} else if (tMin == tMax) {
					tMin = 0;
				}

				if (tMin <= 0) {
					tMin = 0;
				}

				if (r.getLimit() < Float.POSITIVE_INFINITY) {
					tMax = Math.min(tMax, r.getLimit());
					if (tMin > tMax) {
						return 0;
					}
				}

				return rootEx.intersectWhere(r, worldMatrix, this, tMin, tMax, results);
			}
			return 0;
		} finally {
			vars.release();
		}
	}

	private int collideWithBoundingVolume(BoundingVolume bv, Matrix4f worldMatrix, CollisionResults results) {
		BoundingBox bbox;
		if (bv instanceof BoundingSphere) {
			BoundingSphere sphere = (BoundingSphere) bv;
			bbox = new BoundingBox(bv.getCenter().clone(), sphere.getRadius(), sphere.getRadius(), sphere.getRadius());
		} else if (bv instanceof BoundingBox) {
			bbox = new BoundingBox((BoundingBox) bv);
		} else {
			throw new UnsupportedCollisionException();
		}

		bbox.transform(worldMatrix.invert(), bbox);
		return rootEx.intersectWhere(bv, bbox, worldMatrix, this, results);
	}

	@Override
	public int collideWith(Collidable other, Matrix4f worldMatrix, BoundingVolume worldBound, CollisionResults results) {

		if (other instanceof Ray) {
			Ray ray = (Ray) other;
			return collideWithRay(ray, worldMatrix, worldBound, results);
		} else if (other instanceof BoundingVolume) {
			BoundingVolume bv = (BoundingVolume) other;
			return collideWithBoundingVolume(bv, worldMatrix, results);
		} else {
			throw new UnsupportedCollisionException();
		}
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
	}

	@Override
	public void read(JmeImporter im) throws IOException {
	}
}
