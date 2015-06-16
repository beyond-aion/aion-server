package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_LIST;

/**
 * Send when the client requests the friendlist
 * 
 * @author Ben
 */
public class CM_SHOW_FRIENDLIST extends AionClientPacket {

	public CM_SHOW_FRIENDLIST(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();
		if (activePlayer != null) {
			sendPacket(new SM_FRIEND_LIST());
			activePlayer.getFriendList().setIsFriendListSent(true);
		}
	}

}
