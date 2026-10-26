package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.flypath.FlyPathEntry;
import com.aionemu.gameserver.model.templates.flypath.FlightPath;
import com.aionemu.gameserver.model.templates.flypath.WindstreamState;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WINDSTREAM;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.RecallService;
import com.aionemu.gameserver.services.RecallService.CancelReason;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

public class CM_WINDSTREAM extends AionClientPacket {

	/**
	 * Allowed distances between the player and the point of the windstream path the client wants to enter or leave at.
	 */
	private static final float MAX_ENTRY_DISTANCE = 45, MAX_GLIDE_EXIT_DISTANCE = 50, MAX_EXIT_DISTANCE = 30;
	private int teleportId;
	private int progress;
	private WindstreamState state;

	public CM_WINDSTREAM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		teleportId = readD();
		progress = readD();
		int stateId = readD();
		state = WindstreamState.getById(stateId);
		if (state == null)
			LoggerFactory.getLogger(CM_WINDSTREAM.class).warn("Unknown Windstream state #{} was sent from {}", stateId,
				getConnection().getActivePlayer().getPosition());
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (state == null)
			return;
		switch (state) {
			case ENTER -> {
				if (player.isUsingFlightTransporterOrWindstream())
					return;
				// the client asks to enter before its move packet with the flight or glide state arrives and repeats the request until it succeeds
				if (!player.isFlying()) {
					sendPacket(new SM_WINDSTREAM(state, false));
					return;
				}
				if (!canEnter(player)) {
					AuditLogger.log(player, "tried to enter windstream " + teleportId + " at " + progress + " from " + player.getPosition());
					sendPacket(new SM_WINDSTREAM(state, false));
					return;
				}
				player.unsetPlayerMode(PlayerMode.RIDE);
				// unlike retail, which keeps it, a pending summon request is cancelled since accepting it would pull the player off the path mid-flight
				RecallService.getInstance().cancel(player, CancelReason.CANCELLED);
				player.setFlightPath(new FlightPath(FlightPath.Type.WINDSTREAM, teleportId, progress));
				player.unsetState(CreatureState.ACTIVE);
				player.unsetState(CreatureState.GLIDING);
				player.setState(CreatureState.FLYING);
				player.unsetFlyState(FlyState.GLIDING);
				player.setFlyState(FlyState.FLYING);
				player.getLifeStats().triggerFpRestore();
			}
			case ENTERED -> {
				if (player.isUsingFlightPath(FlightPath.Type.WINDSTREAM)) {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.WINDSTREAM, teleportId, progress), true);
					QuestEngine.getInstance().onEnterWindStream(new QuestEnv(null, player, 0), teleportId);
				}
				return; // don't send SM_WINDSTREAM
			}
			case LEAVE_GLIDING, LEAVE -> {
				if (!player.isUsingFlightPath(FlightPath.Type.WINDSTREAM))
					return;
				if (!canLeave(player)) {
					AuditLogger.log(player, "tried to leave windstream " + teleportId + " at " + progress + " from " + player.getPosition());
					sendPacket(new SM_WINDSTREAM(state, false));
					return;
				}
				player.unsetState(CreatureState.FLYING);
				player.setState(CreatureState.ACTIVE);
				player.unsetFlyState(FlyState.FLYING);
				player.unsetFlyState(FlyState.GLIDING);
				player.getMoveController().restartFalling(player.getZ());
				if (state == WindstreamState.LEAVE_GLIDING)
					player.getFlyController().switchToGliding();
				else
					player.getGameStats().updateStatsAndSpeedVisually();
				player.setFlightPath(null);
				PacketSendUtility.broadcastPacket(player,
					new SM_EMOTION(player, state == WindstreamState.LEAVE_GLIDING ? EmotionType.WINDSTREAM_END : EmotionType.WINDSTREAM_EXIT), true);
				if (player.isTransformed()) // send sm_transform if player is transformed
					PacketSendUtility.broadcastPacketAndReceive(player, new SM_TRANSFORM(player));
			}
			case LEFT, RESET -> {
			}
			case START_BOOST, END_BOOST -> {
				if (!player.isUsingFlightPath(FlightPath.Type.WINDSTREAM))
					return;
				boolean start = state == WindstreamState.START_BOOST;
				if (!(start ? player.getFlightPath().startBoost(progress) : player.getFlightPath().endBoost(progress))) {
					sendPacket(new SM_WINDSTREAM(state, false));
					return;
				}
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, start ? EmotionType.WINDSTREAM_START_BOOST : EmotionType.WINDSTREAM_END_BOOST),
					true);
			}
		}
		sendPacket(new SM_WINDSTREAM(state, true));
	}

	private boolean canEnter(Player player) {
		FlyPathEntry path = DataManager.FLY_PATH.getPathTemplate(teleportId / 1000);
		if (path == null || !path.isWindstream() || path.getStartWorldId() != player.getWorldId() || !path.canEnterWindstream(progress))
			return false;
		Point3D entry = path.getWindstreamPosition(progress);
		return PositionUtil.isInRange(player, entry.getX(), entry.getY(), entry.getZ(), MAX_ENTRY_DISTANCE);
	}

	private boolean canLeave(Player player) {
		FlyPathEntry path = DataManager.FLY_PATH.getPathTemplate(player.getFlightPath().getId() / 1000);
		if (path == null || !path.isWindstream())
			return true;
		if (!player.getFlightPath().isProgressReachable(progress))
			return false;
		int boostedProgress = progress + player.getFlightPath().getBoostGain(progress);
		if (!path.canLeaveWindstream(boostedProgress))
			return false;
		Point3D exit = path.getWindstreamPosition(boostedProgress);
		float maxDistance = state == WindstreamState.LEAVE_GLIDING ? MAX_GLIDE_EXIT_DISTANCE : MAX_EXIT_DISTANCE;
		return PositionUtil.isInRange(player, exit.getX(), exit.getY(), exit.getZ(), maxDistance);
	}

}
