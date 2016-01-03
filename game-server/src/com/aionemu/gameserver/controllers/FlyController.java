package com.aionemu.gameserver.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class FlyController {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FlyController.class);

	private static final long FLY_REUSE_TIME = 10000;
	private Player player;

	public FlyController(Player player) {
		this.player = player;
	}

	/**
	 * 
	 */
	public void onStopGliding() {
		if (player.isInGlidingState()) {
			if (player.isInFlyState(FlyState.FLYING)) {
				player.unsetFlyState(FlyState.GLIDING);
				player.unsetState(CreatureState.GLIDING);
				// PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.STOP_GLIDE, 0, 0), true);
			} else {
				player.unsetFlyState(FlyState.GLIDING);
				player.unsetState(CreatureState.GLIDING);
				player.getLifeStats().triggerFpRestore();
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.STOP_GLIDE, 0, 0), true);
			}
			player.getGameStats().updateStatsAndSpeedVisually();
		}
	}

	/**
	 * Ends flying 1) by CM_EMOTION (pageDown or fly button press) 2) from server side during teleportation (abyss gates should not break flying) 3)
	 * when FP is decreased to 0
	 */
	public void endFly(boolean forceEndFly) {
		// unset flying and gliding
		if (player.isFlying()) {
			player.unsetFlyState(FlyState.FLYING);
			player.unsetFlyState(FlyState.GLIDING);
			player.unsetState(CreatureState.FLYING);
			player.unsetState(CreatureState.GLIDING);
			player.unsetState(CreatureState.FLOATING_CORPSE);
			player.getGameStats().updateStatsAndSpeedVisually();

			if (forceEndFly)
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.LAND, 0, 0), true);
			player.getLifeStats().triggerFpRestore();
		}
	}

	/**
	 * This method is called to start flying (called by CM_EMOTION when pageUp or pressed fly button)
	 */
	public boolean startFly() {
		if (player.getFlyReuseTime() > System.currentTimeMillis()) {
			AuditLogger.info(player, "No Flight Cooldown Hack. Reuse time: " + ((player.getFlyReuseTime() - System.currentTimeMillis()) / 1000));
			return false;
		}
		if (!RestrictionsManager.canFly(player))
			return false;
		player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME - 100);
		player.setFlyState(FlyState.FLYING);
		player.setState(CreatureState.FLYING);
		if (player.isInPlayerMode(PlayerMode.RIDE)) {
			player.setState(CreatureState.FLOATING_CORPSE);
		}
		player.getLifeStats().triggerFpReduce();
		player.getGameStats().updateStatsAndSpeedVisually();

		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.FLY, 0, 0), true);
		return true;
	}

	/**
	 * Switching to glide mode (called by CM_MOVE with VALIDATE_GLIDE movement type) 1) from standing state 2) from flying state If from stand to glide
	 * - start fp reduce + emotions/stats if from fly to glide - only emotions/stats
	 */
	public boolean switchToGliding() {
		if (player.isInGlidingState() || !player.canPerformMove())
			return false;

		// check restrictions
		if (!RestrictionsManager.canGlide(player)) {
			return false;
		}
		if (player.getFlyState() == 0) {
			// fly reuse time only if gliding from walking
			if (player.getFlyReuseTime() > System.currentTimeMillis()) {
				return false;
			}
			player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME);
			player.getLifeStats().triggerFpReduce();
		}
		player.setFlyState(FlyState.GLIDING);
		player.setState(CreatureState.GLIDING);

		player.getGameStats().updateStatsAndSpeedVisually();
		return true;
	}
}
