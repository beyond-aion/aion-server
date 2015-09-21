package com.aionemu.gameserver.geoEngine.scene;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResultsEx;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * @author Neon (based on MrPoke & Rolandas' work)
 * @see Node
 */
public class NodeEx extends Node implements SpatialEx {

	protected short collisionFlags = (short) CollisionIntention.ALL.getId();

	public NodeEx(String name) {
		super(name);
	}

	@Override
	public short getCollisionFlags() {
		return collisionFlags;
	}

	@Override
	public void setCollisionFlags(short flags) {
		collisionFlags = flags;
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
		if ((getIntentions() & results.getIntentions()) == 0)
			return 0;

		if (other instanceof Ray || children.size() > 4) {
			BoundingVolume bv = getWorldBound();
			if (bv == null || bv.collideWith(other) == 0)
				return 0;
		}

		int total = 0;

		for (Spatial c : getChildren()) {
			total += c.collideWith(other, results);
			if (total > 0 && results.isOnlyFirst())
				break;
		}

		return total;
	}

	@Override
	public NodeEx clone(boolean cloneMaterials) {
		return (NodeEx) super.clone(cloneMaterials); // collisionFlags/primitives are cloned by value automatically
	}

	@Override
	public NodeEx clone() {
		return clone(true);
	}

	@Override
	public Spatial deepClone() {
		return super.deepClone(); // nothing to be done here for now
	}
}
