package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MACRO_RESULT;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * Request to create
 * 
 * @author SoulKeeper
 */
public class CM_MACRO_CREATE extends AionClientPacket {

	/**
	 * Macro number. Fist is 1, second is 2. Starting from 1, not from 0
	 */
	private int macroPosition;

	/**
	 * XML that represents the macro
	 */
	private String macroXML;

	/**
	 * Constructs new client packet instance.
	 * 
	 * @param opcode
	 */
	public CM_MACRO_CREATE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * Read macro data
	 */
	@Override
	protected void readImpl() {
		macroPosition = readC();
		macroXML = readS();
	}

	@Override
	protected void runImpl() {
		PlayerService.addMacro(getConnection().getActivePlayer(), macroPosition, macroXML);
		sendPacket(SM_MACRO_RESULT.SM_MACRO_CREATED);
	}
}
