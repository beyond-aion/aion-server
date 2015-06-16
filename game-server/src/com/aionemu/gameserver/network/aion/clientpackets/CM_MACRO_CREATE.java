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
 * Request to create
 * 
 * @author SoulKeeper
 */
public class CM_MACRO_CREATE extends AionClientPacket {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_MACRO_CREATE.class);

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

	/**
	 * Logging
	 */
	@Override
	protected void runImpl() {
		log.debug(String.format("Created Macro #%d: %s", macroPosition, macroXML));
		Player activePlayer = getConnection().getActivePlayer();
		
		if(activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_MACROSSES) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		PlayerService.addMacro(getConnection().getActivePlayer(), macroPosition, macroXML);

		sendPacket(SM_MACRO_RESULT.SM_MACRO_CREATED);
	}
}
