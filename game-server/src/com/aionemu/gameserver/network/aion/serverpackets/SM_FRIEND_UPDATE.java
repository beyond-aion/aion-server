package com.aionemu.gameserver.network.aion.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Sent to update a player's status in a friendlist
 * 
 * @author Ben, Neon
 */
public class SM_FRIEND_UPDATE extends AionServerPacket {

	private int friendObjId;

	private static Logger log = LoggerFactory.getLogger(SM_FRIEND_UPDATE.class);

	public SM_FRIEND_UPDATE(int friendObjId) {
		this.friendObjId = friendObjId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Friend f = con.getActivePlayer().getFriendList().getFriend(friendObjId);
		if (f == null)
			log.debug("Attempted to update friend list status of " + friendObjId + " for " + con.getActivePlayer().getName()
				+ " - object ID not found on friend list");
		else {
			writeS(f.getName());
			writeD(f.getLevel());
			writeD(f.getPlayerClass().getClassId());
			writeC(f.getGender().getGenderId());
			writeD(f.getMapId());
			writeD(f.getStatus() == Status.ONLINE ? 0 : f.getLastOnlineEpochSeconds());
			writeS(f.getNote());
			writeC(f.getStatus().getId());
		}
	}
}
