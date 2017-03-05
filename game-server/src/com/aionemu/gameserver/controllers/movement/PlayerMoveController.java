package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.utils.PacketSendUtility;
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

	@Override
	public void abortMove() {
		super.abortMove();
		stopFalling(owner.getZ());
	}

	public void updateFalling(float newZ) {
		if (lastFallZ != 0) {
			fallDistance += lastFallZ - newZ;
			if (fallDistance >= FallDamageConfig.MAXIMUM_DISTANCE_MIDAIR) {
				fallDistance = 0;
				lastFallZ = 0;
				if (!owner.getController().die(TYPE.FALL_DAMAGE, LOG.REGULAR)) // invulnerable players cannot die
					return;
				owner.getController().onStopMove(); // stops and notifies move observers
				if (!owner.isInInstance()) { // instant revive at kisk or bind point
					Kisk kisk = owner.getKisk();
					if (kisk != null && kisk.isActive())
						PlayerReviveService.kiskRevive(owner);
					else
						PlayerReviveService.bindRevive(owner);
					PacketSendUtility.sendPacket(owner, new SM_EMOTION(owner, EmotionType.RESURRECT)); // send to remove res option window
				}
				return;
			}
		}
		lastFallZ = newZ;
		owner.getObserveController().notifyMoveObservers();
	}

	public void stopFalling(float newZ) {
		if (lastFallZ == 0)
			return;

		if (!owner.isFlying()) {
			fallDistance += lastFallZ - newZ;
			int damage = StatFunctions.calculateFallDamage(owner, fallDistance);
			if (damage > 0) {
				owner.getLifeStats().reduceHp(TYPE.FALL_DAMAGE, damage, 0, LOG.REGULAR, owner);
				owner.getObserveController().notifyAttackedObservers(owner, 0);
			}
		}
		fallDistance = 0;
		lastFallZ = 0;
		owner.getObserveController().notifyMoveObservers();
	}
}
