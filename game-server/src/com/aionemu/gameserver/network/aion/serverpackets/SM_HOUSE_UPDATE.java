package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * @author Rolandas
 * @modified Neon
 */
public class SM_HOUSE_UPDATE extends AbstractHouseInfoPacket {

	public SM_HOUSE_UPDATE(House house) {
		super(house);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(1); // unk
		writeH(0);
		writeH(1); // unk

		writeD(0);
		writeD(house.getAddress().getId());
		writeD(house.getOwnerId());
		writeD(house.getBuilding().getType().getId());
		writeC(1); // unk

		writeD(house.getBuilding().getId());
		writeC(house.getHouseOwnerStates());
		writeC(house.getDoorState().getPacketValue());

		writeS(house.getButler() == null ? null : house.getButler().getMasterName(), 52); // owner name

		writeCommonInfo(true);
	}
}
