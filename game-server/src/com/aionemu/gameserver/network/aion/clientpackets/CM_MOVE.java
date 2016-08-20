package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.PlayerMoveController;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.services.antihack.AntiHackService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

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
	private int unk1, unk2;

	public CM_MOVE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		x = readF();
		y = readF();
		z = readF();

		heading = (byte) readC();
		type = (byte) readC();

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
			glideFlag = (byte) readC();
		}
		if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
			unk1 = readD();
			unk2 = readD();
			vehicleX = readF();
			vehicleY = readF();
			vehicleZ = readF();
		}
	}

	@Override
	protected void runImpl() {
		final Player player = getConnection().getActivePlayer();
		if (player == null)
			return;
		WorldPosition pos = player.getPosition();
		if (pos == null || !pos.isSpawned())
			return;
		if (player.getLifeStats().isAlreadyDead())
			return;
		if (player.getEffectController().isUnderFear() || player.isInCustomState(CustomPlayerState.WATCHING_CUTSCENE)) // client sends crap when watching cutscenes in transform state
			return;

		float speed = player.getGameStats().getMovementSpeedFloat();
		PlayerMoveController m = player.getMoveController();
		byte oldMask = m.movementMask;
		m.movementMask = type;

		if (type == MovementMask.IMMEDIATE) { // stopping or turning
			if (oldMask == MovementMask.IMMEDIATE) { // turning
				player.getController().onMove();
			} else {
				if ((oldMask & MovementMask.ABSOLUTE) == MovementMask.ABSOLUTE) { // update target destination coordinates
					m.setNewDirection(x, y, z, heading);
					PacketSendUtility.broadcastToSightedPlayers(player,
						new SM_MOVE(player, (byte) (MovementMask.POSITION | MovementMask.MANUAL | MovementMask.ABSOLUTE)));
				}
			}
			// notify arrived
			player.getController().onStopMove();
			player.getFlyController().onStopGliding();
		} else {
			if ((type & MovementMask.GLIDE) == MovementMask.GLIDE) {
				m.glideFlag = glideFlag;
				player.getFlyController().switchToGliding();
			}

			if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL) { // start move or change direction
				if ((type & MovementMask.ABSOLUTE) == MovementMask.ABSOLUTE) {
					m.setNewDirection(x2, y2, z2, heading);
					if (player.isInCustomState(CustomPlayerState.TELEPORTATION_MODE)) {
						World.getInstance().updatePosition(player, x2, y2, z2, heading);
						m.updateLastMove();
						PacketSendUtility.broadcastToSightedPlayers(player, new SM_MOVE(player), true);
						return;
					}
				} else {
					m.vectorX = vectorX;
					m.vectorY = vectorY;
					m.vectorZ = vectorZ;
					m.setNewDirection(x, y, z, heading);
				}
				if (!m.isInMove())
					player.getController().onStartMove();
			} else {
				player.getController().onMove();
				if ((type & MovementMask.ABSOLUTE) == 0)
					m.setNewDirection(x + m.vectorX * speed, y + m.vectorY * speed, player.isFlying() ? z + m.vectorZ * speed : z + m.vectorZ, heading);
			}

			if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
				m.unk1 = unk1;
				m.unk2 = unk2;
				m.vehicleX = vehicleX;
				m.vehicleY = vehicleY;
				m.vehicleZ = vehicleZ;
			}
		}

		if (!AntiHackService.canMove(player, x, y, z, speed, type))
			return;

		World.getInstance().updatePosition(player, x, y, z, heading);
		m.updateLastMove();

		if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL || type == MovementMask.IMMEDIATE)
			PacketSendUtility.broadcastToSightedPlayers(player, new SM_MOVE(player));

		if ((type & MovementMask.FALL) == MovementMask.FALL) {
			player.getFlyController().onStopGliding();
			m.updateFalling(z);
		} else {
			m.stopFalling(z);
		}
	}

	@Override
	public String toString() {
		return "CM_MOVE [type=" + (type & 0xFF) + ", heading=" + heading + ", x=" + x + ", y=" + y + ", z=" + z + ", x2=" + x2 + ", y2=" + y2 + ", z2="
			+ z2 + ", vehicleX=" + vehicleX + ", vehicleY=" + vehicleY + ", vehicleZ=" + vehicleZ + ", vectorX=" + vectorX + ", vectorY=" + vectorY
			+ ", vectorZ=" + vectorZ + ", glideFlag=" + glideFlag + ", unk1=" + unk1 + ", unk2=" + unk2 + "]";
	}
}
