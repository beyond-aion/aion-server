package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ATracer
 */
public class PlayerMoveController extends PlayableMoveController<Player> {

	private float fallDistance;
	private float lastFallZ;
	private long lastJumpUpdate;
	
	public PlayerMoveController(Player owner) {
		super(owner);
	}
	
	public final void updateLastJump() {
		lastJumpUpdate = System.currentTimeMillis();
	}
	
	@Override
	public boolean isJumping() {
		return System.currentTimeMillis() - lastJumpUpdate < 1000;
	}

	public void updateFalling(float newZ) {
		if (lastFallZ != 0) {
			fallDistance += lastFallZ - newZ;
			if (fallDistance >= FallDamageConfig.MAXIMUM_DISTANCE_MIDAIR) {
				StatFunctions.calculateFallDamage(owner, fallDistance, false);
			}
		}
		lastFallZ = newZ;
		owner.getObserveController().notifyMoveObservers();
	}

	public void stopFalling(float newZ) {
		if (lastFallZ != 0) {
			if (!owner.isFlying()) {
				StatFunctions.calculateFallDamage(owner, fallDistance, true);
			}
			fallDistance = 0;
			lastFallZ = 0;
			owner.getObserveController().notifyMoveObservers();
		}
	}

}
