package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EDIT_CHAR_GENDER_CANT_NO_ITEM;

import java.util.Set;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PlayerAppearanceDAO;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerEnterWorldService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * In this packet the Aion client is requesting edit of a character.
 * 
 * @author IlBuono, Neon
 */
public class CM_CHARACTER_EDIT extends AbstractCharacterEditPacket {

	private int objectId;

	public CM_CHARACTER_EDIT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		readBasicInfo();
		readAppearance();
	}

	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		PlayerAccountData playerAccData = client.getAccount().getPlayerAccountData(objectId);
		if (playerAccData == null || !playerAccData.getPlayerCommonData().isInEditMode())
			return;
		int[] appearanceTickets = { 169650000, 169650001, 169650002, 169650003, 169650004, 169650005, 169650006, 169650007, 169650008 };
		int[] genderTickets = { 169660000, 169660001, 169660002, 169660003, 169660004 };
		PlayerEnterWorldService.enterWorld(client, objectId);
		Player player = client.getActivePlayer();

		boolean isGenderSwitch = player.getGender() != gender;
		int[] ticketIds = isGenderSwitch ? genderTickets : appearanceTickets;
		SM_SYSTEM_MESSAGE errorMsg = isGenderSwitch ? STR_EDIT_CHAR_GENDER_CANT_NO_ITEM() : STR_EDIT_CHAR_GENDER_CANT_NO_ITEM();

		for (int ticketId : ticketIds) {
			if (player.getInventory().decreaseByItemId(ticketId, 1)) {
				errorMsg = null;
				break;
			}
		}
		if (errorMsg != null) {
			PacketSendUtility.sendPacket(player, errorMsg);
			return;
		}

		if (isGenderSwitch)
			player.getCommonData().setGender(gender);
		player.setPlayerAppearance(playerAppearance);
		DAOManager.getDAO(PlayerAppearanceDAO.class).store(player); // save new appearance

		// broadcast new appearance (no need to save gender here, will be saved periodically and on logout)
		player.clearKnownlist();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));
		player.updateKnownlist();
	}
}
