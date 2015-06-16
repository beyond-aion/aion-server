package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MACRO_RESULT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;

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
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_MACRO_DELETE.class);

	/**
	 * Macro id that has to be deleted
	 */
	private int macroPosition;

	/**
	 * Constructs new client packet instance.
	 * 
	 * @param opcode
	 */
	public CM_MACRO_DELETE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * Reading macro id
	 */
	@Override
	protected void readImpl() {
		macroPosition = readC();
	}

	/**
	 * Logging
	 */
	@Override
	protected void runImpl() {
		log.debug("Request to delete macro #" + macroPosition);
		
		Player activePlayer = getConnection().getActivePlayer();
		
		if(activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_MACROSSES) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		PlayerService.removeMacro(getConnection().getActivePlayer(), macroPosition);

		sendPacket(SM_MACRO_RESULT.SM_MACRO_DELETED);
	}
}
