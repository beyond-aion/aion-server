package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dao.PlayerAppearanceDAO;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.player.PlayerEnterWorldService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

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
		readBasicInfo(false);
		readAppearance();
	}

	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		PlayerAccountData playerAccData = client.getAccount().getPlayerAccountData(objectId);
		if (playerAccData == null || !playerAccData.getPlayerCommonData().isInEditMode())
			return;
		PlayerEnterWorldService.enterWorld(client, objectId);
		Player player = client.getActivePlayer();

		boolean isGenderSwitch = player.getGender() != gender;
		if (checkOrRemoveTicket(player, isGenderSwitch, true)) {
			boolean spawnedBeforeAttributesChanged = player.isSpawned(); // just in case CM_LEVEL_READY was sent early
			if (isGenderSwitch)
				player.getCommonData().setGender(gender); // no need to save gender here, will be saved periodically and on logout
			player.setPlayerAppearance(playerAppearance);
			PlayerAppearanceDAO.store(player); // save new appearance
			if (spawnedBeforeAttributesChanged)
				player.getController().onChangedPlayerAttributes();
		} else { // can only happen if you illegally enter the character edit screen
			AuditLogger.log(player, "tried to apply their plastic surgery without a ticket.");
		}
	}

	public static boolean checkOrRemoveTicket(Player player, boolean isGenderSwitch, boolean removeTicket) {
		int[] ticketIds = isGenderSwitch ? new int[] { 169660000, 169660001, 169660002, 169660003, 169660004 } : new int[] { 169650000, 169650001, 169650002, 169650003, 169650004, 169650005, 169650006, 169650007, 169650008 };
		for (int ticketId : ticketIds) {
			if (removeTicket && player.getInventory().decreaseByItemId(ticketId, 1) || !removeTicket && player.getInventory().getItemCountByItemId(ticketId) > 0) {
				return true;
			}
		}
		return false;
	}
}
