package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * Sent in the following cases:<br>
 * - Executing commands prefixed by //// in the command tab of the GM Panel (Shift + F1) if "Builder control (///)" is selected in the settings tab of
 *   the GM Dialog (Shift + G)<br>
 * - Executing commands prefixed by ///// in the command tab of the GM Panel (Shift + F1) if "Builder command (//)" is selected in the settings tab of
 *   the GM Dialog (Shift + G)<br>
 * - Executing commands prefixed by //// in macros if the console has been activated via "\con_disable_console 0" from the command tab of the GM Panel<br>
 */
public class CM_DEBUG_COMMAND extends AbstractGmCommandPacket {

	public CM_DEBUG_COMMAND(int opcode, Set<AionConnection.State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void runImpl() {
		LoggerFactory.getLogger("ADMINAUDIT_LOG").info(getConnection().getActivePlayer() + " sent debug command ////" + command);
	}
}
