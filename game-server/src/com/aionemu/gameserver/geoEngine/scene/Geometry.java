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

import com.aionemu.gameserver.geoEngine.bounding.BoundingVolume;
import com.aionemu.gameserver.geoEngine.collision.Collidable;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Matrix3f;
import com.aionemu.gameserver.geoEngine.math.Matrix4f;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector3f;

public class Geometry extends Spatial {

	/**
	 * The mesh contained herein
	 */
	protected Mesh mesh;

	protected Matrix4f cachedWorldMat = new Matrix4f();

	/**
	 * Do not use this constructor. Serialization purposes only.
	 */
	public Geometry() {
	}

	/**
	 * Create a geometry node without any mesh data.
	 * 
	 * @param name
	 *          The name of this geometry
	 */
	public Geometry(String name) {
		super(name);
	}

	/**
	 * Create a geometry node with mesh data.
	 * 
	 * @param name
	 *          The name of this geometry
	 * @param mesh
	 *          The mesh data for this geometry
	 */
	public Geometry(String name, Mesh mesh) {
		this(name);
		if (mesh == null)
			throw new NullPointerException();

		this.mesh = mesh;
	}

	public int getVertexCount() {
		return mesh.getVertexCount();
	}

	public int getTriangleCount() {
		return mesh.getTriangleCount();
	}

	public void setMesh(Mesh mesh) {

		this.mesh = mesh;
	}

	public Mesh getMesh() {
		return mesh;
	}

	/**
	 * @return The bounding volume of the mesh, in model space.
	 */
	public BoundingVolume getModelBound() {
		return mesh.getBound();
	}

	/**
	 * Updates the bounding volume of the mesh. Should be called when the mesh has been modified.
	 */
	public void updateModelBound() {
		mesh.updateBound();
		worldBound = getModelBound().transform(cachedWorldMat, worldBound);
	}

	public Matrix4f getWorldMatrix() {
		return cachedWorldMat;
	}

	@Override
	public void setModelBound(BoundingVolume modelBound) {
		mesh.setBound(modelBound);
	}

	public int collideWith(Collidable other, CollisionResults results) {
		if (other instanceof Ray) {
			if (!worldBound.intersects(((Ray) other)))
				return 0;
		}
		// NOTE: BIHTree in mesh already checks collision with the
		// mesh's bound
		int prevSize = results.size();
		int added = mesh.collideWith(other, cachedWorldMat, worldBound, results);
		int newSize = results.size();
		for (int i = prevSize; i < newSize; i++)
			results.getCollisionDirect(i).setGeometry(this);
		return added;
	}

	@Override
	public void setTransform(Matrix3f rotation, Vector3f loc, float scale) {
		cachedWorldMat.loadIdentity();
		cachedWorldMat.setRotationMatrix(rotation);
		cachedWorldMat.scale(scale);
		cachedWorldMat.setTranslation(loc);
	}

	@Override
	public short getCollisionFlags() {
		return mesh.getCollisionFlags();
	}

	@Override
	public void setCollisionFlags(short flags) {
		mesh.setCollisionFlags(flags);
	}
}
