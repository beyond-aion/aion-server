package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.drop.DropService;

/**
 * @author alexa026, Correted by Metos, ATracer
 */
public class CM_START_LOOT extends AionClientPacket {

	/**
	 * Target object id that client wants to TALK WITH or 0 if wants to unselect
	 */

	private int targetObjectId;
	private int action;

	/**
	 * Constructs new instance of <tt>CM_CM_REQUEST_DIALOG </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_START_LOOT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();// empty
		action = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (action == 0) // open
		{
			DropService.getInstance().requestDropList(player, targetObjectId);
		} else if (action == 1) // close
		{
			DropService.getInstance().closeDropList(player, targetObjectId);
		}
	}
}
