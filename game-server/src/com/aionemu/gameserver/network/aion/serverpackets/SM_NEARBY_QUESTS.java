package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author MrPoke, Rolandas, Neon
 */
public class SM_NEARBY_QUESTS extends AionServerPacket {

	private static final int notYetAvailableBit = 1 << 17;
	private Map<Integer, Integer> nearbyQuestList;

	public SM_NEARBY_QUESTS(Map<Integer, Integer> nearbyQuestList) {
		this.nearbyQuestList = nearbyQuestList;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(0);
		writeH(-nearbyQuestList.size() & 0xFFFF);
		for (Entry<Integer, Integer> nearbyQuest : nearbyQuestList.entrySet()) {
			int questId = nearbyQuest.getKey();
			if (nearbyQuest.getValue() > 0)
				questId |= notYetAvailableBit; // for transparent/grey quest marker above npc's head
			writeD(questId);
		}
	}
}
