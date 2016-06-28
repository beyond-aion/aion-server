package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

public class SM_QUEST_LIST extends AionServerPacket {

	private List<QuestState> questStates;

	public SM_QUEST_LIST(List<QuestState> questState) {
		this.questStates = questState;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(0x01); // unk
		writeH(-questStates.size() & 0xFFFF);
		for (QuestState qs : questStates) {
			writeD(qs.getQuestId());
			writeC(qs.getStatus().value());
			writeD(qs.getQuestVars().getQuestVars() | (qs.getFlags() << 24));
			writeC(Math.min(qs.getCompleteCount(), 255));
		}
	}
}
