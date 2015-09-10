package com.aionemu.gameserver.geoEngine.scene;

import com.aionemu.gameserver.geoEngine.collision.bih.BIHTreeEx;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Matrix4f;
import com.jme3.scene.CollisionData;
import com.jme3.scene.Mesh;

/**
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see Mesh
 */
public class MeshEx extends Mesh {

	private CollisionData collisionTreeEx = null;
	private short collisionFlags = -1;

	public short getCollisionFlags() {
		return collisionFlags;
	}

	public void setCollisionFlags(short collisionFlags) {
		this.collisionFlags = collisionFlags;
	}

	public byte getMaterialId() {
		return (byte) (collisionFlags & 0xFF);
	}

	public byte getIntentions() {
		return (byte) (collisionFlags >> 8);
	}

	@Override
	public void createCollisionData() {
		BIHTreeEx tree = new BIHTreeEx(this);
		tree.construct();
		collisionTreeEx = tree;
	}

	@Override
	public void clearCollisionData() {
		collisionTreeEx = null;
	}

	@Override
	public int collideWith(Collidable other, Matrix4f worldMatrix, BoundingVolume worldBound, CollisionResults results) {

		if (getVertexCount() == 0) {
			return 0;
		}

		if (collisionTreeEx == null) {
			createCollisionData();
		}

		return collisionTreeEx.collideWith(other, worldMatrix, worldBound, results);
	}

	@Override
	public MeshEx clone() {
		MeshEx clone = (MeshEx) super.clone();
		clone.collisionTreeEx = collisionTreeEx != null ? collisionTreeEx : null;
		return clone;
	}

	@Override
	public MeshEx deepClone() {
		MeshEx clone = (MeshEx) super.deepClone();
		clone.collisionTreeEx = collisionTreeEx != null ? collisionTreeEx : null;
		return clone;
	}
}
