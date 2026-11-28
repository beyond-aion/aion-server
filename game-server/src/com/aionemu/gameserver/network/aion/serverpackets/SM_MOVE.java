package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.controllers.movement.CreatureMoveController;
import com.aionemu.gameserver.controllers.movement.GlideFlag;
import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.PlayableMoveController;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is displaying movement of players etc.
 *
 * @author -Nemesiss-
 */
public class SM_MOVE extends AionServerPacket {

	private final int objectId;
	private final float x, y, z;
	private final byte heading;
	private final byte movementMask;
	private final boolean playable;
	private final float targetX, targetY, targetZ;
	private final float vectorX, vectorY, vectorZ;
	private final byte glideFlag;
	private final int windBoxId;
	private final int vehicleId, vehiclePartId;
	private final float vehicleX, vehicleY, vehicleZ;

	public SM_MOVE(Creature creature) {
		this(creature, creature.getMoveController().getMovementMask());
	}

	public SM_MOVE(Creature creature, byte movementMask) {
		this(creature, movementMask, null);
	}

	/**
	 * Takes the position and movement state at construction, so a packet still in the send queue is not overwritten by the next move of the
	 * same creature.
	 *
	 * @param vector the movement vector of a move the server drives itself, or null for the one the client reported last
	 */
	public SM_MOVE(Creature creature, byte movementMask, Vector3f vector) {
		CreatureMoveController<?> mc = creature.getMoveController();
		PlayableMoveController<?> pmc = mc instanceof PlayableMoveController ? (PlayableMoveController<?>) mc : null;
		this.objectId = creature.getObjectId();
		this.x = creature.getX();
		this.y = creature.getY();
		this.z = creature.getZ();
		this.heading = creature.getHeading();
		this.movementMask = movementMask;
		this.playable = pmc != null;
		this.targetX = mc.getTargetX2();
		this.targetY = mc.getTargetY2();
		this.targetZ = mc.getTargetZ2();
		if (vector != null) {
			this.vectorX = vector.getX();
			this.vectorY = vector.getY();
			this.vectorZ = vector.getZ();
		} else if (pmc != null) {
			this.vectorX = pmc.vectorX;
			this.vectorY = pmc.vectorY;
			this.vectorZ = pmc.vectorZ;
		} else {
			this.vectorX = 0;
			this.vectorY = 0;
			this.vectorZ = 0;
		}
		this.glideFlag = pmc == null ? 0 : pmc.glideFlag;
		this.windBoxId = pmc == null ? 0 : pmc.windBoxId;
		this.vehicleId = pmc == null ? 0 : pmc.vehicleId;
		this.vehiclePartId = pmc == null ? 0 : pmc.vehiclePartId;
		this.vehicleX = pmc == null ? 0 : pmc.vehicleX;
		this.vehicleY = pmc == null ? 0 : pmc.vehicleY;
		this.vehicleZ = pmc == null ? 0 : pmc.vehicleZ;
	}

	@Override
	protected void writeImpl(AionConnection client) {
		writeD(objectId);
		writeF(x);
		writeF(y);
		writeF(z);
		writeC(heading);

		writeC(movementMask);

		if ((movementMask & MovementMask.POSITION) == MovementMask.POSITION && (movementMask & MovementMask.MANUAL) == MovementMask.MANUAL) {
			if (playable && (movementMask & MovementMask.ABSOLUTE) == 0) {
				writeF(vectorX);
				writeF(vectorY);
				writeF(vectorZ);
			} else {
				writeF(targetX);
				writeF(targetY);
				writeF(targetZ);
			}
		}
		if ((movementMask & MovementMask.GLIDE) == MovementMask.GLIDE) {
			// the same layout as in CM_MOVE
			writeC(glideFlag | windBoxId >> 8);
			if ((glideFlag & GlideFlag.WIND_BOX) != 0)
				writeC(windBoxId);
		}
		if (playable && (movementMask & MovementMask.VEHICLE) == MovementMask.VEHICLE) {
			writeD(vehicleId);
			writeD(vehiclePartId);
			// the position relative to that part, which is not the direction he moves in
			writeF(vehicleX);
			writeF(vehicleY);
			writeF(vehicleZ);
		}
	}
}
