package com.aionemu.gameserver.network.aion.serverpackets;

import javolution.util.FastTable;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author MrPoke
 */
public class SM_QUEST_COMPLETED_LIST extends AionServerPacket {

	private final int unk;
	private final boolean negative;
	private FastTable<QuestState> questState;

	public SM_QUEST_COMPLETED_LIST(FastTable<QuestState> questState) {
		this.unk = 0x01;
		this.negative = true;
		this.questState = questState;
	}

	public SM_QUEST_COMPLETED_LIST(int unk, boolean negative, FastTable<QuestState> questState) {
		this.unk = unk;
		this.negative = negative;
		this.questState = questState;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(unk); // 2.1
		writeH((negative ? -1 : 1) * (questState.size() & 0xFFFF));
		//QuestsData QUEST_DATA = DataManager.QUEST_DATA;
		for (QuestState qs : questState) {
			writeD(qs.getQuestId());
			writeC(qs.getCompleteCount());
			// complete count after the counter reset procedure
			writeC(qs.canRepeat() ? 0 : 1);
		}

		questState = null;
	}
}
