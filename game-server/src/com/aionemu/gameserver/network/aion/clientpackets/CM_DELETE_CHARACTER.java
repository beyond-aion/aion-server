package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dao.PlayerPasskeyDAO;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.CharacterPasskey.ConnectType;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHARACTER_SELECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_CHARACTER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
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

	public CM_DELETE_CHARACTER(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		playOk2 = readD();
		chaOid = readD();
	}

	@Override
	protected void runImpl() {
		Account account = getConnection().getAccount();
		PlayerAccountData playerAccData = account.getPlayerAccountData(chaOid);
		if (playerAccData == null)
			return;
		if (LegionService.getInstance().getLegionMember(playerAccData.getPlayerCommonData()) != null) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_STAYMODE_CANCEL_1());
			return;
		}
		// passkey check
		if (SecurityConfig.PASSKEY_ENABLE && !account.getCharacterPasskey().isPass()) {
			account.getCharacterPasskey().setConnectType(ConnectType.DELETE);
			account.getCharacterPasskey().setObjectId(chaOid);
			boolean hasPasskey = PlayerPasskeyDAO.existCheckPlayerPasskey(account.getId());
			sendPacket(new SM_CHARACTER_SELECT(hasPasskey ? 1 : 0));
		} else {
			PlayerService.deletePlayer(playerAccData);
			sendPacket(new SM_DELETE_CHARACTER(chaOid, playerAccData.getDeletionTimeInSeconds()));
		}
	}
}
