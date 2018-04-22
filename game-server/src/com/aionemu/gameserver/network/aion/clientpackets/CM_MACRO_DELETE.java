package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MACRO_RESULT;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * Packet that is responsible for macro deletion.<br>
 * Client sends id in the macro list.<br>
 * For instance client has 4 macros and we are going to delete macro #3.<br>
 * Client sends request to delete macro #3.<br>
 * And macro #4 becomes macro #3.<br>
 * So we have to use a list to store macros properly.
 * 
 * @author SoulKeeper
 */
public class CM_MACRO_DELETE extends AionClientPacket {

	/**
	 * Macro id that has to be deleted
	 */
	private int macroPosition;

	/**
	 * Constructs new client packet instance.
	 * 
	 * @param opcode
	 */
	public CM_MACRO_DELETE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	/**
	 * Reading macro id
	 */
	@Override
	protected void readImpl() {
		macroPosition = readUC();
	}

	@Override
	protected void runImpl() {
		PlayerService.removeMacro(getConnection().getActivePlayer(), macroPosition);
		sendPacket(SM_MACRO_RESULT.SM_MACRO_DELETED);
	}
}
