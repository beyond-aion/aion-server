package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class FlyController {

	private static final long FLY_REUSE_TIME = 10000;
	private final Player player;

	public FlyController(Player player) {
		this.player = player;
	}

	public void onStopGliding() {
		if (player.isInGlidingState()) {
			player.unsetFlyState(FlyState.GLIDING);
			player.unsetState(CreatureState.GLIDING);
			if (!player.isInFlyState(FlyState.FLYING)) {
				player.getLifeStats().triggerFpRestore();
			} else {
				player.getLifeStats().triggerFpReduce();
			}
			player.getGameStats().updateStatsAndSpeedVisually();
		}
	}

	/**
	 * Called for every move of the player once the position was taken over, not just for glide moves: a gliding player can cross the border of
	 * a flight area on any packet, and the client expects the free flight to end at that moment.
	 */
	public void onMove() {
		if (player.isInGlidingState() && player.isInFlyingState() && !player.isInCustomState(CustomPlayerState.FREE_FLY_MODE)
			&& !player.getWorldMapInstance().isInsideFlyZone(player.getX(), player.getY(), player.getZ()))
			switchToNormalGlide();
	}

	/**
	 * @return True if the player is in free flight and the given position is outside the flight areas of the world.
	 */
	public boolean isFlyingOutOfFlyZone(float x, float y, float z) {
		return player.isInFlyingState() && !player.getWorldMapInstance().isInsideFlyZone(x, y, z);
	}

	/**
	 * Ends free flight but keeps the glide, the way a gliding player who leaves a flight area is handled: the client gets S_ACTION 48 and the
	 * stats update, keeps gliding and can finish the landing anywhere.
	 */
	private void switchToNormalGlide() {
		player.unsetFlyState(FlyState.FLYING);
		player.unsetState(CreatureState.FLYING);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.broadcastToSightedPlayers(player, new SM_EMOTION(player, EmotionType.STOP_FLY), true);
	}

	/**
	 * Ends flying 1) by CM_EMOTION (pageDown or fly button press) 2) from server side during teleport (abyss gates should not break flying) 3)
	 * when FP is decreased to 0
	 */
	public void endFly(boolean broadcastPacket) {
		player.unsetFlyState(FlyState.FLYING);
		player.unsetFlyState(FlyState.GLIDING);
		player.unsetState(CreatureState.FLYING);
		player.unsetState(CreatureState.GLIDING);
		player.unsetState(CreatureState.FLOATING_CORPSE);
		player.setFlightPath(null);
		player.getMoveController().restartFalling(player.getZ());
		player.getGameStats().updateStatsAndSpeedVisually();

		if (broadcastPacket && player.isSpawned())
			PacketSendUtility.broadcastToSightedPlayers(player, new SM_EMOTION(player, EmotionType.LAND), true);
		player.getLifeStats().triggerFpRestore();
	}

	/**
	 * This method is called to start flying (called by CM_EMOTION when pageUp or pressed fly button, on revive or after teleport in some cases)
	 * 
	 * @param broadcastPacket
	 *          - notify the players client, and all players in range that he started flying
	 * @param ignoreFlightCooldown
	 *          - if true, this will skip cooldown check and not set a new cooldown
	 * @return False if the player could not start flying due to some restriction, true otherwise (this method does not consider if the player was
	 *         already flying).
	 */
	public boolean startFly(boolean broadcastPacket, boolean ignoreFlightCooldown) {
		if (!canFly(player))
			return false;
		if (!ignoreFlightCooldown) {
			if (player.getFlyReuseTime() > System.currentTimeMillis()) {
				AuditLogger.log(player, "possibly using fly cooldown hack. Left cooldown time: " + ((player.getFlyReuseTime() - System.currentTimeMillis()) / 1000) + "s");
				return false;
			}
			startFlightCooldown();
		}
		player.setFlyState(FlyState.FLYING);
		player.setState(CreatureState.FLYING);
		if (player.isInPlayerMode(PlayerMode.RIDE)) {
			player.setState(CreatureState.FLOATING_CORPSE);
		}
		player.getLifeStats().triggerFpReduce();
		player.getGameStats().updateStatsAndSpeedVisually();

		if (broadcastPacket)
			PacketSendUtility.broadcastToSightedPlayers(player, new SM_EMOTION(player, EmotionType.FLY), true);
		return true;
	}

	private void startFlightCooldown() {
		player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME - 100);
	}

	private static boolean canFly(Player player) {
		if (player.isUsingFlightTransporterOrWindstream())
			return false;
		if (!player.getCommonData().isDaeva()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN());
			return false;
		}
		if (!player.isInCustomState(CustomPlayerState.FREE_FLY_MODE) && !player.isInsideFlyZone()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLYING_FORBIDDEN_HERE());
			return false;
		}
		if (player.getEffectController().isAbnormalSet(AbnormalState.NOFLY)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_FLY_NOW_DUE_TO_NOFLY());
			return false;
		}
		if (player.getTransformModel().cantFly()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLY_CANNOT_FLY_POLYMORPH_STATUS());
			return false;
		}
		return player.getStore() == null;
	}

	/**
	 * Switching to glide mode (called by CM_MOVE with VALIDATE_GLIDE movement type) 1) from standing state 2) from flying state If from stand to glide
	 * - start fp reduce + emotions/stats if from fly to glide - only emotions/stats
	 */
	public boolean switchToGliding() {
		if (player.isInGlidingState() || player.isUsingFlightTransporterOrWindstream())
			return false;
		if (!canGlide(player)) {
			// the client starts a glide on its own when it cannot fly, so close the wings instead of only refusing
			if (!canFlyAtAll(player) && player.isFlying())
				endFly(true);
			return false;
		}
		startFlightCooldown();
		player.setFlyState(FlyState.GLIDING);
		player.setState(CreatureState.GLIDING);
		player.getLifeStats().triggerFpReduce();
		player.getGameStats().updateStatsAndSpeedVisually();
		return true;
	}

	/**
	 * Ends the wind path the player was carried along: the glide exit keeps him gliding, a fall exit or a player who cannot fly drops him. The
	 * cooldown is started on the glide exit because the client shows a fresh one there. Stricter than retail, which glides even a player who cannot fly
	 * and lets its client force the glide; such a player falls here, so the caller closes his wings.
	 *
	 * @return true if he keeps gliding
	 */
	public boolean leaveWindstream(boolean glideExit) {
		boolean gliding = glideExit && canFlyAtAll(player);
		player.unsetState(CreatureState.FLYING);
		player.unsetFlyState(FlyState.FLYING);
		player.unsetFlyState(FlyState.GLIDING);
		player.setState(CreatureState.ACTIVE);
		player.setFlightPath(null);
		player.getMoveController().restartFalling(player.getZ());
		if (gliding) {
			startFlightCooldown();
			player.setFlyState(FlyState.GLIDING);
			player.setState(CreatureState.GLIDING);
			player.getLifeStats().triggerFpReduce();
		} else // the client clears the cooldown when falling out of the windstream path (e.g. by closing wings)
			player.setFlyReuseTime(0);
		player.getGameStats().updateStatsAndSpeedVisually();
		return gliding;
	}

	/**
	 * @return false if the player cannot fly at all: a form that cannot fly or a NOFLY effect
	 */
	private static boolean canFlyAtAll(Player player) {
		return !player.getTransformModel().cantFly() && !player.getEffectController().isAbnormalSet(AbnormalState.NOFLY);
	}

	private static boolean canGlide(Player player) {
		if (!player.getCommonData().isDaeva()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN());
			return false;
		}
		if (!canFlyAtAll(player)) {
			PacketSendUtility.sendPacket(player, player.getTransformModel().cantFly() ? SM_SYSTEM_MESSAGE.STR_GLIDE_CANNOT_GLIDE_POLYMORPH_STATUS()
				: SM_SYSTEM_MESSAGE.STR_GLIDE_CANNOT_GLIDE_ABNORMAL_STATUS());
			return false;
		}
		if (!player.canPerformMove()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_CANNOT_GLIDE_ABNORMAL_STATUS());
			return false;
		}
		if (!player.isFlying() && player.getFlyReuseTime() > System.currentTimeMillis()) {
			PacketSendUtility.broadcastToSightedPlayers(player, new SM_EMOTION(player, EmotionType.STOP_GLIDE), true);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLYING_TIME_NOT_READY());
			return false;
		}
		return true;
	}
}
