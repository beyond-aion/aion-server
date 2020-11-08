package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author MrPoke, Neon
 */
public class SM_QUEST_COMPLETED_LIST extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 4;
	public static final Function<QuestState, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (questState) -> 6;

	private final int updateMode;
	private final List<QuestState> questStates;

	/**
	 * @param updateMode
	 *          0 = rewrite all entries, 1 = insert new entries
	 * @param questStates
	 *          list of affected quests
	 */
	public SM_QUEST_COMPLETED_LIST(int updateMode, List<QuestState> questStates) {
		this.updateMode = updateMode;
		this.questStates = questStates;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(1); // unk, always 1 (when 0, no entries change)
		writeC(updateMode); // 0 = rewrite all entries, 1 = insert new entries
		writeH(-questStates.size() & 0xFFFF);
		for (QuestState qs : questStates) {
			writeD(qs.getQuestId());
			writeC(Math.min(qs.getCompleteCount(), 255));
			writeC(qs.canRepeat() ? 0 : 1); // wrong! most times equal to the complete count on retail (else 0), not clear what it is
		}
	}
}
