package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * The quests the player has completed, with how often. Long lists are split over several packets, the first one replaces the client's list and
 * the others add to it.
 *
 * @author MrPoke, Neon
 */
public class SM_QUEST_COMPLETED_LIST extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 4;
	public static final Function<QuestState, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (questState) -> 6;

	private final int updateMode;
	private final List<QuestState> questStates;

	/**
	 * @param updateMode
	 *          0 = replace all entries, 1 = add the entries
	 * @param questStates
	 *          list of affected quests
	 */
	public SM_QUEST_COMPLETED_LIST(int updateMode, List<QuestState> questStates) {
		this.updateMode = updateMode;
		this.questStates = questStates;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(1); // the player's own list, 0 would fill the list of quests a GM looks at
		writeC(updateMode); // 0 = replace all entries, otherwise add them
		writeH(-questStates.size() & 0xFFFF); // negative: the last part of the list
		for (QuestState qs : questStates) {
			writeD(qs.getQuestId());
			writeC(Math.min(qs.getCompleteCount(), 255));
			// completions since the repeat limit was last reset; the client only reads it for the GM list, so 1 or 0 is close enough
			writeC(qs.canRepeat() ? 0 : 1);
		}
	}
}
