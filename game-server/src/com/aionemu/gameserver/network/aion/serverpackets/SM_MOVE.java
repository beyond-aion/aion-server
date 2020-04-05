package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.controllers.movement.CreatureMoveController;
import com.aionemu.gameserver.controllers.movement.GlideFlag;
import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.PlayableMoveController;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is displaying movement of players etc.
 *
 * @author -Nemesiss-
 */
public class SM_MOVE extends AionServerPacket {

	/**
	 * Object that is moving.
	 */
	private Creature creature;
	private byte movementMask;

	public SM_MOVE(Creature creature) {
		this(creature, creature.getMoveController().getMovementMask());
	}

	public SM_MOVE(Creature creature, byte movementMask) {
		this.creature = creature;
		this.movementMask = movementMask;
	}

	@Override
	protected void writeImpl(AionConnection client) {
		CreatureMoveController<?> mc = creature.getMoveController();
		PlayableMoveController<?> pmc = mc instanceof PlayableMoveController ? (PlayableMoveController<?>) mc : null;
		writeD(creature.getObjectId());
		writeF(creature.getX());
		writeF(creature.getY());
		writeF(creature.getZ());
		writeC(creature.getHeading());

		writeC(movementMask);

		if ((movementMask & MovementMask.POSITION) == MovementMask.POSITION && (movementMask & MovementMask.MANUAL) == MovementMask.MANUAL) {
			if (pmc != null && (movementMask & MovementMask.ABSOLUTE) == 0) {
				writeF(pmc.vectorX);
				writeF(pmc.vectorY);
				writeF(pmc.vectorZ);
			} else {
				writeF(mc.getTargetX2());
				writeF(mc.getTargetY2());
				writeF(mc.getTargetZ2());
			}
		}
		if ((movementMask & MovementMask.GLIDE) == MovementMask.GLIDE) {
			byte glideFlag = pmc == null ? 0 : pmc.glideFlag;
			writeC(glideFlag);
			if (glideFlag == GlideFlag.GEYSER)
				writeC(pmc.geyserLocationId);
		}
		if (pmc != null && (movementMask & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
			writeD(pmc.unk1);
			writeD(pmc.unk2);
			writeF(pmc.vectorX);
			writeF(pmc.vectorY);
			writeF(pmc.vectorZ);
		}
	}
}
