package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * The quests the player is working on, sent on login. The client keeps up to 266 of them.
 */
public class SM_QUEST_LIST extends AionServerPacket {

	private final List<QuestState> questStates;

	public SM_QUEST_LIST(List<QuestState> questStates) {
		this.questStates = questStates;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(1); // the player's own list, 0 would fill the list of quests a GM looks at
		writeC(0); // part of a split list, 0 replaces the client's list
		writeH(-questStates.size() & 0xFFFF); // negative: the last part of the list
		for (QuestState qs : questStates) {
			writeD(qs.getQuestId());
			writeC(qs.getStatus().value());
			writeD(qs.getQuestVars().getQuestVars()); // all six quest vars
			writeC(0); // quest branch, which we don't have (the complete count goes with SM_QUEST_COMPLETED_LIST)
		}
	}
}
