package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.controllers.FlyController;
import com.aionemu.gameserver.controllers.movement.GlideFlag;
import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.PlayerMoveController;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.flypath.FlightPath;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLINK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_POSITION;
import com.aionemu.gameserver.services.antihack.AntiHackService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;

/**
 * Packet about player movement.
 *
 * @author -Nemesiss-
 */
public class CM_MOVE extends AionClientPacket {

	private byte type;
	private byte heading;
	private float x, y, z, x2, y2, z2, vehicleX, vehicleY, vehicleZ, vectorX, vectorY, vectorZ;
	private byte glideFlag;
	private int vehicleId, vehiclePartId;
	private int windBoxId;

	public CM_MOVE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		x = readF();
		y = readF();
		z = readF();

		heading = readC();
		type = readC();

		if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL) {
			if ((type & MovementMask.ABSOLUTE) == MovementMask.ABSOLUTE) {
				x2 = readF();
				y2 = readF();
				z2 = readF();
			} else {
				vectorX = readF();
				vectorY = readF();
				vectorZ = readF();
				x2 = vectorX + x;
				y2 = vectorY + y;
				z2 = vectorZ + z;
			}
		}
		if ((type & MovementMask.GLIDE) == MovementMask.GLIDE) {
			int first = readUC(); // the flag in the upper half, with a wind box the top four bits of its twelve bit id in the lower half
			glideFlag = (byte) (first & 0xF0);
			windBoxId = (glideFlag & GlideFlag.WIND_BOX) == 0 ? 0 : (first & 0x0F) << 8 | readUC();
		}
		if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
			vehicleId = readD();
			vehiclePartId = readD();
			vehicleX = readF();
			vehicleY = readF();
			vehicleZ = readF();
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isDead() || player.getEffectController().isUnderFear() || player.getEffectController().isConfused()) // just in case of bad timing
			return;
		if (handleBogusPacket(player))
			return;
		if (handleFlyingOutOfFlyZone(player))
			return;
		if (player.isUsingFlightPath(FlightPath.Type.WINDSTREAM)) {
			// a wind path ends at its last point without a leave packet, so the first ordinary move means the path is over
			player.getFlyController().leaveWindstream(false);
		}

		PlayerMoveController m = player.getMoveController();
		boolean jumping = false;
		byte oldMask = m.movementMask;
		m.movementMask = type;

		if (type == MovementMask.IMMEDIATE) { // stopping or turning
			m.setNewDirection(x, y, z, heading);
		} else {
			jumping = !player.isFlying() && (type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL
					&& (type & MovementMask.ABSOLUTE) != MovementMask.ABSOLUTE && (type & MovementMask.GLIDE) != MovementMask.GLIDE
					&& (type & MovementMask.VEHICLE) != MovementMask.VEHICLE && z2 > z;
			if ((type & MovementMask.GLIDE) == MovementMask.GLIDE) {
				m.glideFlag = glideFlag;
				m.windBoxId = windBoxId;
				player.getFlyController().switchToGliding();
			} else // a move without the glide flag ends a running glide
				player.getFlyController().onStopGliding();

			if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL) { // start move or change direction
				m.setNewDirection(x2, y2, z2, heading);
				if ((type & MovementMask.ABSOLUTE) == MovementMask.ABSOLUTE) {
					if (player.isInCustomState(CustomPlayerState.TELEPORTATION_MODE)) {
						player.getMoveController().setIsJumping(false);
						World.getInstance().updatePosition(player, x2, y2, z2, heading);
						m.onMoveFromClient();
						PacketSendUtility.broadcastToSightedPlayers(player, new SM_POSITION(player), true);
						return;
					}
				} else {
					m.vectorX = vectorX;
					m.vectorY = vectorY;
					m.vectorZ = vectorZ;
				}
			} else {
				if ((type & MovementMask.ABSOLUTE) == 0) {
					//The movement vector from the client already accounts for movement speed, so multiplying it by speed again overestimates the distance several times.
					m.setNewDirection(x + m.vectorX, y + m.vectorY, z + m.vectorZ, heading);
				} else if (heading != player.getHeading())
					m.setNewDirection(m.getTargetX2(), m.getTargetY2(), m.getTargetZ2(), heading);
			}

			if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
				m.vehicleId = vehicleId;
				m.vehiclePartId = vehiclePartId;
				m.vehicleX = vehicleX;
				m.vehicleY = vehicleY;
				m.vehicleZ = vehicleZ;
			}
		}

		if (!AntiHackService.canMove(player, x, y, z, type)) {
			player.getMoveController().setIsJumping(false);
			return;
		}

		if (!player.isSpawned()) // should be checked as late as possible, to prevent false warnings from World.updatePosition
			return;
		if (player.isProtectionActive() && (player.getX() != x || player.getY() != y || player.getZ() > z + 0.5f))
			player.getController().stopProtectionActiveTask();
		player.getMoveController().setIsJumping(jumping);
		World.getInstance().updatePosition(player, x, y, z, heading);
		player.getFlyController().onMove();
		m.onMoveFromClient();
		notifyControllers(player, oldMask);

		if (isSeenByOthers())
			PacketSendUtility.broadcastToSightedPlayers(player, new SM_MOVE(player));

		if ((type & MovementMask.FALL) == MovementMask.FALL) {
			if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
				m.restartFalling(z);
			} else {
				player.getFlyController().onStopGliding();
				m.updateFalling(z);
			}
		} else {
			m.stopFalling(z);
		}
	}

	/**
	 * Puts a flying player back to the position the server has when a move reports him outside a flight area. Clients can legitimately send such moves
	 * when touching the ground on the border of a fly zone or when the GM free flight mode was disabled during flight outside a fly zone.<br>
	 * Glide moves are handled by {@link FlyController#onMove()}: the player keeps gliding there, only the free flight ends.
	 *
	 * @return True if the move was refused and the player was put back.
	 */
	private boolean handleFlyingOutOfFlyZone(Player player) {
		boolean gliding = (type & MovementMask.GLIDE) == MovementMask.GLIDE && (type & (MovementMask.POSITION | MovementMask.FALL)) != 0;
		if (gliding || player.isInCustomState(CustomPlayerState.FREE_FLY_MODE) || !player.getFlyController().isFlyingOutOfFlyZone(x, y, z))
			return false;
		player.getMoveController().setIsJumping(false);
		if (!player.getWorldMapInstance().isInsideFlyZone(player.getX(), player.getY(), player.getZ())) // the player was flying there in free flight mode before
			player.getFlyController().endFly(true);
		PacketSendUtility.sendPacket(player,
			new SM_BLINK(player.getX(), player.getY(), player.getZ(), player.getHeading(), SM_BLINK.BlinkReason.FLY_ON_NO_FLY_AREA));
		return true;
	}

	/**
	 * While running or falling the client keeps reporting its position on its own. Those reports are not passed on, every other move is: a
	 * start or turn (position and manual flags together), a stop, also on a vehicle, and a glide change.
	 */
	private boolean isSeenByOthers() {
		boolean startOrTurn = (type & (MovementMask.POSITION | MovementMask.MANUAL)) == (MovementMask.POSITION | MovementMask.MANUAL);
		boolean runningOrFalling = (type & (MovementMask.POSITION | MovementMask.FALL)) != 0;
		return startOrTurn || !runningOrFalling;
	}

	private boolean handleBogusPacket(Player player) {
		if (player.isInCustomState(CustomPlayerState.WATCHING_CUTSCENE)) // client sends crap during cutscenes in transformed state
			return true;
		VisibleObject target = player.getTarget();
		if (target != null && player.getMoveController().hasMovedByRandomMoveLocEffect() && PositionUtil.isInRange(target, x, y, z, 2)
			&& !PositionUtil.isInRange(player, x, y, z, 3)) {
			/*
			 * The game client often sends incorrect coordinates and tries to move you to your target's position when using any RandomMoveLocEffect
			 * (Emergency Teleport I, Power: Emergency Teleport I, Blind Leap, Feint, etc.) while:
			 * 1) running or jumping around the corner of an obstacle
			 * 2) jumping on an obstacle
			 * 3) jumping over an obstacle (harder to reproduce with skills that have no animation time)
			 * 4) running up/down the upper end of stairs: only works for skills with animation time, animation must either start or end at the top flat level
			 * 5) Additionally, teleporting across any type of crest blocking line of sight between the start and end position causes a similar condition.
			 * It seems like this happens if the game thinks you have passed through an obstacle while using teleportation skills. Server side positions
			 * are not considered for this, it is all evaluated by the client based on local coordinates.
			 * Most often incorrect coordinates are contained in the first move packet after SM_CASTSPELL_RESULT, but sometimes it's the second one or,
			 * in case of teleportation skills with animation time, sometimes even both. That's when we also see type == 0. Other times, type often has
			 * MovementType.FALL but not always (especially if a directional teleport was involved).
			 * Sending a move packet with the current server-side position works around this client bug and the client will not move you to your target's
			 * position.
			 */
			boolean moveForcefully = type == 0 || (type & MovementMask.MANUAL) == MovementMask.MANUAL && (type & MovementMask.POSITION) == MovementMask.POSITION;
			sendPacket(moveForcefully ? new SM_FORCED_MOVE(player, player) : new SM_MOVE(player));
			return true;
		}
		return false;
	}

	private void notifyControllers(Player player, byte oldMovementMask) {
		if (player.getMoveController().getMovementMask() == MovementMask.IMMEDIATE) { // stopping or turning
			if (oldMovementMask == MovementMask.IMMEDIATE) // turning
				player.getController().onMove();
			// notify arrived
			player.getController().onStopMove();
			player.getFlyController().onStopGliding();
		} else if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL
			&& !player.getMoveController().isInMove()) { // start move or change direction
			player.getController().onStartMove();
		} else {
			player.getController().onMove();
		}
	}

	@Override
	public String toString() {
		return "CM_MOVE [type=" + (type & 0xFF) + ", heading=" + heading + ", x=" + x + ", y=" + y + ", z=" + z + ", x2=" + x2 + ", y2=" + y2 + ", z2="
			+ z2 + ", vehicleX=" + vehicleX + ", vehicleY=" + vehicleY + ", vehicleZ=" + vehicleZ + ", vectorX=" + vectorX + ", vectorY=" + vectorY
			+ ", vectorZ=" + vectorZ + ", glideFlag=" + glideFlag + ", vehicleId=" + vehicleId + ", vehiclePartId=" + vehiclePartId + "]";
	}
}
