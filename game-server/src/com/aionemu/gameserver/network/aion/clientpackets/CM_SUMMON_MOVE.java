package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.controllers.movement.CreatureMoveController;
import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.SummonMoveController;
import com.aionemu.gameserver.model.gameobjects.Creature;
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

	private int objectId;
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
		objectId = readD();
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
		Creature summonOrMercenary = player.getSummonOrMercenary(objectId);
		if (summonOrMercenary == null || !summonOrMercenary.isSpawned())
			return;
		EffectController effectController = summonOrMercenary.getEffectController();
		if (effectController.isInAnyAbnormalState(AbnormalState.CANT_MOVE_STATE) || effectController.isUnderFear() || effectController.isConfused())
			return;
		CreatureMoveController<? extends Creature> m = summonOrMercenary.getMoveController();
		m.movementMask = type;

		if (m instanceof SummonMoveController smc && (type & MovementMask.GLIDE) == MovementMask.GLIDE) {
			smc.glideFlag = glideFlag;
		}

		if (type == MovementMask.IMMEDIATE) {
			summonOrMercenary.getController().onStopMove();
		} else if ((type & MovementMask.POSITION) == MovementMask.POSITION && (type & MovementMask.MANUAL) == MovementMask.MANUAL) {
			if ((type & MovementMask.ABSOLUTE) == 0) // skip position update since the server has already set the correct position for stun or resist
				return;
			summonOrMercenary.getMoveController().setNewDirection(x2, y2, z2, heading);
			summonOrMercenary.getController().onStartMove();
		} else
			summonOrMercenary.getController().onMove();

		if (m instanceof SummonMoveController smc && (type & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
			smc.unk1 = unk1;
			smc.unk2 = unk2;
			smc.vehicleX = vehicleX;
			smc.vehicleY = vehicleY;
			smc.vehicleZ = vehicleZ;
		}
		World.getInstance().updatePosition(summonOrMercenary, x, y, z, heading);
		m.updateLastMove();

		if ((type & MovementMask.POSITION) == MovementMask.POSITION || type == MovementMask.IMMEDIATE)
			PacketSendUtility.broadcastToSightedPlayers(summonOrMercenary, new SM_MOVE(summonOrMercenary));
	}
}
