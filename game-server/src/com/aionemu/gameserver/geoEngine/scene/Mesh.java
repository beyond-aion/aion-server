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

package com.aionemu.gameserver.geoEngine.scene;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.bounding.BoundingVolume;
import com.aionemu.gameserver.geoEngine.collision.Collidable;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.collision.bih.BIHTree;
import com.aionemu.gameserver.geoEngine.math.Matrix4f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.mesh.IndexArray;

public class Mesh {

	/**
	 * The bounding volume that contains the mesh entirely. By default a BoundingBox (AABB).
	 */
	private BoundingVolume meshBound = new BoundingBox();

	private CollisionData collisionTree = null;

	private float[] vertices;
	private IndexArray indices;

	private byte materialId = 0;
	private byte collisionIntentions = 0;

	public Mesh() {
	}

	public int getTriangleCount() {
		return indices.size() / 3;
	}

	public int getVertexCount() {
		return vertices.length;
	}

	public void getTriangle(int index, Vector3f v1, Vector3f v2, Vector3f v3) {
		index *= 3;
		int vertexIndex = indices.get(index++) * 3;
		v1.x = vertices[vertexIndex++];
		v1.y = vertices[vertexIndex++];
		v1.z = vertices[vertexIndex];
		vertexIndex = indices.get(index++) * 3;
		v2.x = vertices[vertexIndex++];
		v2.y = vertices[vertexIndex++];
		v2.z = vertices[vertexIndex];
		vertexIndex = indices.get(index) * 3;
		v3.x = vertices[vertexIndex++];
		v3.y = vertices[vertexIndex++];
		v3.z = vertices[vertexIndex];
	}

	public void swapTriangles(int i1, int i2) {
		indices.swap(i1, i2);
	}

	public void createCollisionData() {
		if (collisionTree != null) {
			return;
		}
		BIHTree tree = new BIHTree(this);
		tree.construct();
		collisionTree = tree;
	}

	public int collideWith(Collidable other, Matrix4f worldMatrix, BoundingVolume worldBound, CollisionResults results) {

		if (collisionTree == null) {
			createCollisionData();
		}

		return collisionTree.collideWith(other, worldMatrix, worldBound, results);
	}

	public void setVertices(FloatBuffer vertices) {
		this.collisionTree = null;
		this.vertices = new float[vertices.limit()];
		vertices.get(this.vertices);
	}

	public void setIndices(Buffer indices) {
		this.collisionTree = null;
		this.indices = IndexArray.from(indices);
	}

	public void updateBound() {
		meshBound.computeFromPoints(FloatBuffer.wrap(vertices));
	}

	public BoundingVolume getBound() {
		return meshBound;
	}

	public void setBound(BoundingVolume modelBound) {
		meshBound = modelBound;
	}

	public void setCollisionIntentions(byte collisionIntentions) {
		this.collisionIntentions = collisionIntentions;
	}

	public void setMaterialId(byte materialId) {
		this.materialId = materialId;
	}

	public byte getCollisionIntentions() {
		return this.collisionIntentions;
	}

	public int getMaterialId() {
		return (this.materialId & 0xFF);
	}

}
