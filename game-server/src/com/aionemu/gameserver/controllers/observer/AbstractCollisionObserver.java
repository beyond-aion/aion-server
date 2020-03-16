package com.aionemu.gameserver.controllers.observer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author MrPoke
 * @moved Rolandas
 */
public abstract class AbstractCollisionObserver extends ActionObserver {

	protected Creature creature;
	protected Vector3f oldPos;
	protected Spatial geometry;
	protected byte intentions;
	private final CheckType checkType;
	private AtomicBoolean isRunning = new AtomicBoolean();

	public AbstractCollisionObserver(Creature creature, Spatial geometry, byte intentions, CheckType checkType) {
		super(ObserverType.MOVE_OR_DIE);
		this.creature = creature;
		this.geometry = geometry;
		WorldPosition lastPos;
		if (creature instanceof Player && (lastPos = ((Player) creature).getMoveController().getLastPositionFromClient()) != null)
			this.oldPos = new Vector3f(lastPos.getX(), lastPos.getY(), lastPos.getZ());
		else
			this.oldPos = new Vector3f(creature.getX(), creature.getY(), creature.getZ());
		this.intentions = intentions;
		this.checkType = checkType;
	}

	@Override
	public void moved() {
		if (!isRunning.getAndSet(true)) {
			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					try {
						Vector3f pos;
						Vector3f dir;
						if (checkType == CheckType.TOUCH) { // check if we are standing on the geometry (either top or bottom)
							float x = creature.getX();
							float y = creature.getY();
							float z = creature.getZ();
							float zMax = z + 0.05f + creature.getObjectTemplate().getBoundRadius().getUpper();
							float zMin = z - 0.11f;
							if (creature instanceof Player) {
								if (((Player) creature).getMoveController().isJumping() || !((Player) creature).isInGlidingState() && !creature.isFlying()) {
									float geoZ = GeoService.getInstance().getZ(creature.getWorldId(), x, y, z, creature.getInstanceId());
									if (!Float.isNaN(geoZ)) {
										zMin = geoZ - 0.11f;
									}
								}
							}
							pos = new Vector3f(x, y, zMax);
							dir = new Vector3f(pos.getX(), pos.getY(), zMin);
						} else { // check if we passed the geometry (either entering or leaving)
							pos = new Vector3f(creature.getX(), creature.getY(), creature.getZ() + GeoMap.COLLISION_CHECK_Z_OFFSET);
							dir = oldPos.clone();
							dir.setZ(dir.getZ() + GeoMap.COLLISION_CHECK_Z_OFFSET);
						}
						Float limit = pos.distance(dir);
						dir.subtractLocal(pos).normalizeLocal();
						Ray r = new Ray(pos, dir);
						r.setLimit(limit);
						CollisionResults results = new CollisionResults(intentions, creature.getInstanceId(), true);
						geometry.collideWith(r, results);
						onMoved(results);
						oldPos = pos;
					} finally {
						isRunning.set(false);
					}
				}
			});
		}
	}

	public abstract void onMoved(CollisionResults result);

	public enum CheckType {
		TOUCH,
		PASS
	}
}
