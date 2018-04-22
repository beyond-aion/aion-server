package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.drop.DropService;

/**
 * @author alexa026, corrected by Metos, ATracer
 */
public class CM_START_LOOT extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_START_LOOT.class);
	/**
	 * Target object id that client wants to TALK WITH or 0 if wants to unselect
	 */
	private int targetObjectId;
	private byte action;

	/**
	 * Constructs new instance of <tt>CM_CM_REQUEST_DIALOG</tt> packet
	 * 
	 * @param opcode
	 */
	public CM_START_LOOT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD(); // empty
		action = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		switch (action) {
			case 0: // open
				DropService.getInstance().requestDropList(player, targetObjectId);
				break;
			case 1: // close
				DropService.getInstance().closeDropList(player, targetObjectId);
				break;
			default:
				log.warn(player + " sent unknown loot action type " + action);
		}
	}
}
