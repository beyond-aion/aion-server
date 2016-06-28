package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author MrPoke, Rolandas
 * @modified Neon
 */
public class SM_NEARBY_QUESTS extends AionServerPacket {

	private Map<Integer, Integer> nearbyQuestList;

	public SM_NEARBY_QUESTS(Map<Integer, Integer> nearbyQuestList) {
		this.nearbyQuestList = nearbyQuestList;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(0);
		writeH(-nearbyQuestList.size() & 0xFFFF);
		for (Entry<Integer, Integer> nearbyQuest : nearbyQuestList.entrySet()) {
			writeH(nearbyQuest.getKey()); // quest id (max 65535, because of short data type, so most event quests are invalid)
			writeH(nearbyQuest.getValue() > 0 ? 2 : 0); // 0 = visible, 1 = hidden, 2 = not yet available (transparent/grey quest marker above npc's head)
		}
	}
}
