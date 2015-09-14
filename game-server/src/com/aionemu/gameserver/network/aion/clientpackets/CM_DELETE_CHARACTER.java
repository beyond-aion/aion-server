package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dao.PlayerPasskeyDAO;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.account.CharacterPasskey.ConnectType;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHARACTER_SELECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_CHARACTER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * In this packets aion client is requesting deletion of character.
 * 
 * @author -Nemesiss-
 */
public class CM_DELETE_CHARACTER extends AionClientPacket {

	/**
	 * PlayOk2 - we dont care...
	 */
	@SuppressWarnings("unused")
	private int playOk2;
	/**
	 * ObjectId of character that should be deleted.
	 */
	private int chaOid;

	/**
	 * Constructs new instance of <tt>CM_DELETE_CHARACTER </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_DELETE_CHARACTER(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		playOk2 = readD();
		chaOid = readD();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		PlayerAccountData playerAccData = client.getAccount().getPlayerAccountData(chaOid);
		if (playerAccData != null && !playerAccData.isLegionMember()) {
			// passkey check
			if (SecurityConfig.PASSKEY_ENABLE && !client.getAccount().getCharacterPasskey().isPass()) {
				client.getAccount().getCharacterPasskey().setConnectType(ConnectType.DELETE);
				client.getAccount().getCharacterPasskey().setObjectId(chaOid);
				boolean isExistPasskey = DAOManager.getDAO(PlayerPasskeyDAO.class).existCheckPlayerPasskey(client.getAccount().getId());

				if (!isExistPasskey)
					client.sendPacket(new SM_CHARACTER_SELECT(0));
				else
					client.sendPacket(new SM_CHARACTER_SELECT(1));
			} else if (getConnection().getAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_DELETE_CHARACTERS) {
				client.sendPacket(SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
				client.sendPacket(new SM_MESSAGE(0, null,
					"Account hacking attempt detected. You can't use this function. Please, contact your server support.", ChatType.GOLDEN_YELLOW));
				return;
			} else {
				PlayerService.deletePlayer(playerAccData);
				client.sendPacket(new SM_DELETE_CHARACTER(chaOid, playerAccData.getDeletionTimeInSeconds()));
			}
		} else {
			client.sendPacket(SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_STAYMODE_CANCEL_1);
		}
	}
}
