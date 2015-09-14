package com.aionemu.gameserver.geoEngine.scene;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResultsEx;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;

/**
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see Geometry
 */
public class GeometryEx extends Geometry implements SpatialEx {

	public GeometryEx(String name) {
		super(name);
	}

	public GeometryEx(String name, MeshEx mesh) {
		super(name, mesh);
	}

	@Override
	public short getCollisionFlags() {
		return mesh != null ? ((MeshEx) mesh).getCollisionFlags() : 0;
	}

	@Override
	public void setCollisionFlags(short flags) {
		if (mesh != null)
			((MeshEx) mesh).setCollisionFlags(flags);
	}

	@Override
	public int collideWith(Collidable other, CollisionResults results) {
		try {
			return collideWith(other, ((CollisionResultsEx) results));
		} catch (ClassCastException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public int collideWith(Collidable other, CollisionResultsEx results) {
		// not used materialIds do not have collision intention for materials set
		// not all material meshes have physical collisions set
		// TODO: implement event mesh collisions
		if ((getIntentions() & results.getIntentions()) == 0 || (getIntentions() & CollisionIntention.EVENT.getId()) != 0)
			return 0;

		if ((results.getIntentions() & CollisionIntention.MATERIAL.getId()) != 0 && getMaterialId() <= 0)
			return 0;

		if (other instanceof Ray) {
			BoundingVolume bv = getWorldBound();
			if (bv == null || !bv.intersects((Ray) other))
				return 0;
		}

		return super.collideWith(other, results);
	}

	@Override
	public GeometryEx clone(boolean cloneMaterials) {
		return (GeometryEx) super.clone(cloneMaterials);
	}

	@Override
	public GeometryEx clone() {
		return clone(true);
	}

	@Override
	public Spatial deepClone() {
		return super.deepClone(); // nothing to be done here for now
	}

	/**
	 * Only accepts instances of {@link MeshEx}! Otherwise an {@link AssertionError} is thrown.
	 */
	@Override
	public void setMesh(Mesh mesh) {
		try {
			setMesh((MeshEx) mesh);
		} catch (ClassCastException e) {
			throw new AssertionError(e);
		}
	}

  /**
   * @see Geometry#setMesh(Mesh)
   */
	public void setMesh(MeshEx mesh) {
		super.setMesh(mesh);
	}
}
