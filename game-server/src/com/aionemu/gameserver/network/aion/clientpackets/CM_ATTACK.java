package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author alexa026, Avol, ATracer, KID
 */
public class CM_ATTACK extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_ATTACK.class);
	/** Object id of the attacked creature */
	private int targetObjectId;
	/** Auto attack counter the client keeps per character, not per target, wrapping at 256. Unused, since we answer with our own counter. */
	@SuppressWarnings("unused")
	private int attackno;
	/** Milliseconds until the auto attack lands, from the animation marker of the equipped weapon plus the flight time of its projectile */
	private int time;
	/** Number of the attack animation the client played, 1 or 2, with an unrelated flag in the highest bit */
	private int type;

	public CM_ATTACK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
		attackno = readUC();
		time = readUH();
		type = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.isDead())
			return;

		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();

		VisibleObject obj = player.getKnownList().getObject(targetObjectId);
		if (obj instanceof Creature) {
			player.getController().attackTarget((Creature) obj, time, type & 0x7F);
		} else if (obj != null) {
			log.warn(player + " attacking unsupported target " + obj);
		}
	}
}
