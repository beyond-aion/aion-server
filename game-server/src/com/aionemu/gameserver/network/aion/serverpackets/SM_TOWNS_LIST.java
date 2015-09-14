package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;

import com.aionemu.gameserver.model.town.Town;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ViAl
 */
public class SM_TOWNS_LIST extends AionServerPacket {

	private Map<Integer, Town> towns;

	public SM_TOWNS_LIST(Map<Integer, Town> towns) {
		this.towns = towns;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(towns.size());
		for (Town town : towns.values()) {
			writeD(town.getId());
			writeD(town.getLevel());
			writeD((int) (town.getLevelUpDate().getTime() / 1000));
		}
	}
}
