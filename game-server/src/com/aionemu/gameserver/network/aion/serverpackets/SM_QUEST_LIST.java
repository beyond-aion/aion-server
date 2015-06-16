package com.aionemu.gameserver.network.aion.serverpackets;

import javolution.util.FastList;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

public class SM_QUEST_LIST extends AionServerPacket {

	private FastList<QuestState> questState;

	public SM_QUEST_LIST(FastList<QuestState> questState) {
		this.questState = questState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {

		writeH(0x01); // unk
		writeH(-questState.size() & 0xFFFF);

		for (QuestState qs : questState) {
			writeD(qs.getQuestId());
			writeC(qs.getStatus().value());
			writeD(qs.getQuestVars().getQuestVars() | (qs.getFlags() << 24));
			writeC(qs.getCompleteCount());
		}
		FastList.recycle(questState);
		questState = null;
	}
}
