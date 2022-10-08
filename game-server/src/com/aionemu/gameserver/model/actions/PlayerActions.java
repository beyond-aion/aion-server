package com.aionemu.gameserver.model.actions;

import java.util.List;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.player.InRoll;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.ride.RideInfo;
import com.aionemu.gameserver.model.templates.windstreams.WindstreamPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class PlayerActions {

	public static boolean isInPlayerMode(Player player, PlayerMode mode) {
		switch (mode) {
			case RIDE:
				return player.ride != null;
			case IN_ROLL:
				return player.inRoll != null;
			case WINDSTREAM:
				return player.windstreamPath != null;
		}
		return false;
	}

	public static void setPlayerMode(Player player, PlayerMode mode, Object obj) {
		switch (mode) {
			case RIDE:
				player.ride = (RideInfo) obj;
				break;
			case IN_ROLL:
				player.inRoll = (InRoll) obj;
				break;
			case WINDSTREAM:
				player.windstreamPath = (WindstreamPath) obj;
				break;
		}
	}

	public static boolean unsetPlayerMode(Player player, PlayerMode mode) {
		switch (mode) {
			case RIDE:
				if (player.ride == null)
					return false;
				player.ride = null;
				// check for sprinting when forcefully dismounting player
				if (player.isInSprintMode()) {
					if (!player.isInFlyingState())// if player is flying while dismounting, do not start restore task
						player.getLifeStats().triggerFpRestore();
					player.setSprintMode(false);
				}
				player.unsetState(CreatureState.RESTING);
				player.unsetState(CreatureState.FLOATING_CORPSE);
				player.setState(CreatureState.ACTIVE);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.CHANGE_SPEED, 0, 0), true);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RIDE_END), true);

				player.getGameStats().updateStatsAndSpeedVisually();

				// remove rideObservers
				List<ActionObserver> rideObservers = player.getRideObservers();
				synchronized (rideObservers) {
					for (ActionObserver observer : rideObservers) {
						player.getObserveController().removeObserver(observer);
					}
					rideObservers.clear();
				}
				return true;
			case IN_ROLL:
				if (player.inRoll == null)
					return false;
				player.inRoll = null;
				return true;
			case WINDSTREAM:
				if (player.windstreamPath == null)
					return false;
				player.windstreamPath = null;
				return true;
			default:
				return false;
		}
	}

}
