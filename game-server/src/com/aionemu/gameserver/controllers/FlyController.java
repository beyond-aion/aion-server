package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
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
	private Player player;

	public FlyController(Player player) {
		this.player = player;
	}

	public void onStopGliding() {
		if (player.isInGlidingState()) {
			player.unsetFlyState(FlyState.GLIDING);
			player.unsetState(CreatureState.GLIDING);
			if (!player.isInFlyState(FlyState.FLYING)) {
				player.getLifeStats().triggerFpRestore();
				PacketSendUtility.broadcastToSightedPlayers(player, new SM_EMOTION(player, EmotionType.STOP_GLIDE), true);
			} else {
				player.getLifeStats().triggerFpReduce();
			}
			player.getGameStats().updateStatsAndSpeedVisually();
		}
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
			player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME - 100);
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

	private static boolean canFly(Player player) {
		if (!player.getCommonData().isDaeva()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN());
			return false;
		}
		if (!player.hasAccess(AdminConfig.FREE_FLIGHT) && (player.isInsideZoneType(ZoneType.NO_FLY) || !player.isInsideZoneType(ZoneType.FLY))) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLYING_FORBIDDEN_HERE());
			return false;
		}
		if (player.getEffectController().isAbnormalSet(AbnormalState.NOFLY)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_FLY_NOW_DUE_TO_NOFLY());
			return false;
		}
		if (player.getTransformModel().getRes6() == 1) {
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
		if (player.isInGlidingState() || !player.canPerformMove())
			return false;

		if (!canGlide(player))
			return false;
		if (player.getFlyState() == 0) {
			// fly reuse time only if gliding from walking
			if (player.getFlyReuseTime() > System.currentTimeMillis()) {
				return false;
			}
			player.setFlyReuseTime(System.currentTimeMillis() + FLY_REUSE_TIME);
		}
		player.setFlyState(FlyState.GLIDING);
		player.setState(CreatureState.GLIDING);
		player.getLifeStats().triggerFpReduce();
		player.getGameStats().updateStatsAndSpeedVisually();
		return true;
	}

	private static boolean canGlide(Player player) {
		if (!player.getCommonData().isDaeva()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN());
			return false;
		}
		if (player.getTransformModel().getRes6() == 1) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_CANNOT_GLIDE_POLYMORPH_STATUS());
			return false;
		}
		return true;
	}
}
