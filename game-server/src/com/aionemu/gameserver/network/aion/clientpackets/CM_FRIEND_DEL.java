package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SocialService;

/**
 * @author Ben
 */
public class CM_FRIEND_DEL extends AionClientPacket {

	private String targetName;

	public CM_FRIEND_DEL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetName = readS();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		Friend friend = activePlayer.getFriendList().getFriend(targetName);
		if (friend == null) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_BUDDYLIST_NOT_IN_LIST());
		} else {
			SocialService.deleteFriend(activePlayer, friend);
		}
	}
}
