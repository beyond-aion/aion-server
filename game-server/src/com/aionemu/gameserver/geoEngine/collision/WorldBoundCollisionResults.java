package com.aionemu.gameserver.geoEngine.collision;

import com.aionemu.gameserver.geoEngine.bounding.BoundingVolume;

/**
 * @author Neon
 */
public class WorldBoundCollisionResults extends CollisionResults {

	private boolean isIncludeWorldBoundCollision;
	private CollisionResult boxCenterPlaneCollision;

	public WorldBoundCollisionResults(CollisionResults parent, BoundingVolume worldBound) {
		super(parent.getIntentions(), parent.getInstanceId(), parent.isOnlyFirst());
		setCanSeeCheck(parent.isCanSeeCheck());
		isIncludeWorldBoundCollision = worldBound.isTreeCollidable();
	}

	public boolean shouldAddBoxCenterPlaneCollision() {
		return isCanSeeCheck() && isIncludeWorldBoundCollision;
	}

	public int addValidCollisionsTo(CollisionResults results) {
		if (isIncludeWorldBoundCollision) {
			if (isCanSeeCheck()) {
				if (boxCenterPlaneCollision != null) {
					results.addCollision(boxCenterPlaneCollision);
					return 1;
				}
			} else {
				forEach(results::addCollision);
				return size();
			}
		}
		return 0;
	}

	public CollisionResult getCenterPlaneContactPoint() {
		return boxCenterPlaneCollision;
	}

	public void setBoxCenterPlaneCollision(CollisionResult boxCenterPlaneCollision) {
		this.boxCenterPlaneCollision = boxCenterPlaneCollision;
	}
}
