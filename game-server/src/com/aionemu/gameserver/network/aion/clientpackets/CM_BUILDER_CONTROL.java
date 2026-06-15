package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * Sent in the following cases:<br>
 * - Clicking buttons in the GM Panel (Shift + F1) if "Builder control (///)" is selected in the settings tab of the GM Dialog (Shift + G)<br>
 * - Executing commands prefixed by /// (optional) in the command tab of the GM Panel (Shift + F1)<br>
 * - Executing commands prefixed by /// in macros if the console has been activated via "\con_disable_console 0" from the command tab of the GM Panel<br>
 * 
 * @author ginho1
 */
public class CM_BUILDER_CONTROL extends AbstractGmCommandPacket {

	public CM_BUILDER_CONTROL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}
}
