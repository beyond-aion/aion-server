package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.BlockedPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;

/**
 * @author Ben
 */
public class CM_BLOCK_SET_REASON extends AionClientPacket {

	String targetName;
	String reason;

	public CM_BLOCK_SET_REASON(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetName = readS();
		reason = readS();

	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		BlockedPlayer target = activePlayer.getBlockList().getBlockedPlayer(targetName);

		if (target == null)
			sendPacket(SM_SYSTEM_MESSAGE.STR_BLOCKLIST_NOT_IN_LIST());
		else {
			SocialService.setBlockedReason(activePlayer, target, reason);
		}
	}
}
