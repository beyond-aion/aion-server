package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingService;

/**
 * Sends a friend list to the client
 * 
 * @author Ben
 * @modified Neon
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
			writeD(friend.getLastOnlineTime()); // Date friend was last online as a Unix timestamp.
			writeS(friend.getNote()); // Friend note
			writeC(friend.getStatus().getId());

			int address = HousingService.getInstance().getPlayerAddress(friend.getObjectId());
			if (address > 0) {
				House house = HousingService.getInstance().getPlayerStudio(friend.getObjectId());
				if (house == null) {
					house = HousingService.getInstance().getHouseByAddress(address);
					writeD(house.getAddress().getId());
				} else {
					writeD(address);
				}
				writeC(house.getDoorState().getPacketValue());
			} else {
				writeD(0);
				writeC(0);
			}

			writeS(friend.getFriendMemo());
		}
	}
}
