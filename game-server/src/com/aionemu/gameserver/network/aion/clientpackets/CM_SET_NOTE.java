package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_LIST;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

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

	@Override
	protected void readImpl() {
		note = readS();

	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		if (!note.equals(activePlayer.getCommonData().getNote())) {
			activePlayer.getCommonData().setNote(note);

			for (Friend friend : activePlayer.getFriendList()) {
				Player friendPlayer = World.getInstance().findPlayer(friend.getObjectId());
				if (friendPlayer != null)
					PacketSendUtility.sendPacket(friendPlayer, new SM_FRIEND_LIST()); // Send him a new friend list packet
			}

		}
	}
}
