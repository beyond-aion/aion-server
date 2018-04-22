package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_VIEW_PLAYER_DETAILS;

/**
 * @author Avol
 */
public class CM_VIEW_PLAYER_DETAILS extends AionClientPacket {

	private int targetObjectId;

	public CM_VIEW_PLAYER_DETAILS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Player target = player.getKnownList().getPlayer(targetObjectId);
		if (target == null)
			return;

		if (!target.getPlayerSettings().isInDeniedStatus(DeniedStatus.VIEW_DETAILS) || player.hasAccess(AdminConfig.VIEW_PLAYER_DETAILS))
			sendPacket(new SM_VIEW_PLAYER_DETAILS(target.getEquipment().getEquippedItemsWithoutStigma(), target));
		else
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_WATCH(target.getName()));
	}
}
