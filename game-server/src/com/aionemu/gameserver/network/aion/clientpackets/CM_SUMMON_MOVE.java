package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.SummonMoveController;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class CM_SUMMON_MOVE extends AionClientPacket {

	private byte type;
	private byte heading;
	private float x, y, z, x2, y2, z2, vehicleX, vehicleY, vehicleZ;
	private byte glideFlag;
	private int unk1, unk2;

	public CM_SUMMON_MOVE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readD(); // object id

		x = readF();
		y = readF();
		z = readF();

		heading = readC();
		type = readC();

		if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL) {
			if ((type & MovementMask.ABSOLUTE) == 0) {
				// this type is sent when the summon is in move and it receives or resists movement restricting effects, like stun, stagger, etc.
				// summon's x/y/z is expected to be immediately updated to the sent x/y/z values and no vector or x2/y2/z2 coords are sent
			} else {
				x2 = readF();
				y2 = readF();
				z2 = readF();
			}
		}
		if ((type & MovementMask.GLIDE) == MovementMask.GLIDE) {
			glideFlag = readC();
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
		Player player = getConnection().getActivePlayer();
		Summon summon = player.getSummon();
		if (summon == null || !summon.isSpawned())
			return;
		if (summon.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_MOVE_STATE) || summon.getEffectController().isUnderFear() || summon.getEffectController().isConfused())
			return;
		SummonMoveController m = summon.getMoveController();
		m.movementMask = type;

		if ((type & MovementMask.GLIDE) == MovementMask.GLIDE) {
			m.glideFlag = glideFlag;
		}

		if (type == MovementMask.IMMEDIATE) {
			summon.getController().onStopMove();
		} else if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL) {
			if ((type & MovementMask.ABSOLUTE) == 0) // skip position update since the server has already set the correct position for stun or resist
				return;
			summon.getMoveController().setNewDirection(x2, y2, z2, heading);
			summon.getController().onStartMove();
		} else
			summon.getController().onMove();

		if ((type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
			m.unk1 = unk1;
			m.unk2 = unk2;
			m.vehicleX = vehicleX;
			m.vehicleY = vehicleY;
			m.vehicleZ = vehicleZ;
		}
		World.getInstance().updatePosition(summon, x, y, z, heading);
		m.updateLastMove();

		if ((type & MovementMask.POSITION) == MovementMask.POSITION || type == MovementMask.IMMEDIATE)
			PacketSendUtility.broadcastPacket(summon, new SM_MOVE(summon));
	}
}
