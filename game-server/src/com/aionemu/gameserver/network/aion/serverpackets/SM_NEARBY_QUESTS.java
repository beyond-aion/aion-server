package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author MrPoke, Rolandas
 */

public class SM_NEARBY_QUESTS extends AionServerPacket {

	private Map<Integer, Integer> nearbyQuestList;

	public SM_NEARBY_QUESTS(Map<Integer, Integer> nearbyQuestList) {
		this.nearbyQuestList = nearbyQuestList;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (nearbyQuestList == null || con.getActivePlayer() == null)
			return;

		writeC(0);
		writeH(-nearbyQuestList.size() & 0xFFFF);
		for (Entry<Integer, Integer> nearbyQuest : nearbyQuestList.entrySet()) {
			if (nearbyQuest.getValue() > 0) {
				writeH(nearbyQuest.getKey());
				writeH(0x2); // To show grey icons for future quests
			} else {
				// Quests are displayed on map
				writeD(nearbyQuest.getKey());
			}
		}
	}
}
