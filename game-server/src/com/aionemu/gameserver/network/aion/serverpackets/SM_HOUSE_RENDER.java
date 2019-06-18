package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * @author Rolandas, Neon
 */
public class SM_HOUSE_RENDER extends AbstractHouseInfoPacket {

	public SM_HOUSE_RENDER(House house) {
		super(house);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeCommonInfo();
	}
}
