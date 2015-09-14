package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CREATE_CHARACTER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NICKNAME_CHECK_RESPONSE;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.Util;

/**
 * In this packets aion client is asking if given nickname is ok/free?.
 * 
 * @author -Nemesiss-
 * @modified cura
 */
public class CM_CHECK_NICKNAME extends AionClientPacket {

	/**
	 * nick name that need to be checked
	 */
	private String nick;

	/**
	 * Constructs new instance of <tt>CM_CHECK_NICKNAME </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_CHECK_NICKNAME(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		nick = readS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		AionConnection client = getConnection();

		nick = Util.convertName(nick);

		if (!PlayerService.isFreeName(nick) || PlayerService.isOldName(nick)) {
			if (GSConfig.CHARACTER_CREATION_MODE == 2)
				client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(SM_CREATE_CHARACTER.RESPONSE_NAME_RESERVED));
			else
				client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(SM_CREATE_CHARACTER.RESPONSE_NAME_ALREADY_USED));
		} else if (!NameRestrictionService.isValidName(nick)) {
			client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(SM_CREATE_CHARACTER.RESPONSE_INVALID_NAME));
		} else if (NameRestrictionService.isForbiddenWord(nick)) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_FORBIDDEN_CHAR_NAME));
		} else {
			client.sendPacket(new SM_NICKNAME_CHECK_RESPONSE(SM_CREATE_CHARACTER.RESPONSE_OK));
		}
	}
}
