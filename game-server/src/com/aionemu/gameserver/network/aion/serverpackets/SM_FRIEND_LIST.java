package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingService;

/**
 * Sends a friend list to the client
 * 
 * @author Ben, Neon
 */
public class SM_FRIEND_LIST extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		FriendList list = con.getActivePlayer().getFriendList();

		writeH(-list.getSize());
		writeC(0);// unk

		for (Friend friend : list) {
			writeD(friend.getObjectId());
			writeS(friend.getName());
			writeD(friend.getLevel());
			writeD(friend.getPlayerClass().getClassId());
			writeC(friend.getGender().getGenderId());
			writeD(friend.getMapId());
			writeD(friend.getStatus() == Status.ONLINE ? 0 : friend.getLastOnlineEpochSeconds());
			writeS(friend.getNote()); // Friend note
			writeC(friend.getStatus().getId());

			House house = HousingService.getInstance().findActiveHouse(friend.getObjectId());
			writeD(house == null ? 0 : house.getAddress().getId());
			writeC(house == null ? 0 : house.getDoorState().getId());

			writeS(friend.getFriendMemo());
		}
	}
}
