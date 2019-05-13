package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HousePermissions;
import com.aionemu.gameserver.model.house.HouseStatus;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * @author Rolandas
 * @modified Neon
 */
public class SM_HOUSE_RENDER extends AbstractHouseInfoPacket {

	public SM_HOUSE_RENDER(House house) {
		super(house);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		boolean isInactive = house.getStatus() == HouseStatus.INACTIVE;

		writeD(0);
		writeD(house.getAddress().getId());
		writeD(isInactive ? 0 : house.getOwnerId());
		writeD(house.getBuilding().getType().getId());
		writeC(1); // unk

		writeD(house.getBuilding().getId());
		writeC(isInactive ? 4 : house.getHouseOwnerStates()); // TODO: 0x4 is wrong in the enum. unk 2 or 3 without owner, 5 or 3 with owner
		writeC(isInactive ? HousePermissions.DOOR_CLOSED.getPacketValue() : house.getDoorState().getPacketValue());

		writeS(isInactive || house.getButler() == null ? null : house.getButler().getMasterName(), 52); // owner name

		writeCommonInfo(!isInactive);
	}
}
