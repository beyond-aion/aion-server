package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_LIST;

/**
 * Received when a player sets his note
 * 
 * @author Ben
 */
public class CM_SET_NOTE extends AionClientPacket {

	private String note;

	public CM_SET_NOTE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		note = readS();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		if (!note.equals(activePlayer.getCommonData().getNote())) {

			activePlayer.getCommonData().setNote(note);

			for (Friend friend : activePlayer.getFriendList()) // For all my friends
			{
				Player frienPlayer = friend.getPlayer();
				if (friend.isOnline() && frienPlayer != null) // If the player is online
				{
					friend.getPlayer().getClientConnection().sendPacket(new SM_FRIEND_LIST()); // Send him a new friend list
																																											// packet
				}
			}

		}
	}
}
