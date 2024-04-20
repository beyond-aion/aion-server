package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas, Neon
 */
public class SM_QUEST_REPEAT extends AionServerPacket {

	List<Integer> repeatableQuests;

	public SM_QUEST_REPEAT(List<Integer> repeatableQuests) {
		this.repeatableQuests = repeatableQuests;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(repeatableQuests.size());
		for (Integer questId : repeatableQuests)
			writeD(questId);

		// There are following messages after this packet:
		// You can receive the daily quest. - STR_MSG_QUEST_LIMIT_RESET_DAILY = 1400854
		// You can receive the daily quest again at %0 in the morning. - STR_MSG_QUEST_LIMIT_START_DAILY = 1400855
		// You can receive the weekly quest. - STR_MSG_QUEST_LIMIT_RESET_WEEK = 1400856
		// You can receive the weekly quest again at %1 in the morning on %0. - STR_MSG_QUEST_LIMIT_START_WEEK = 1400857
	}
}
