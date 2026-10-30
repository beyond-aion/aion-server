package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestExtraCategory;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * Changes one quest on the client. The first byte says what happens to it, the quest id always follows.
 *
 * @author VladimirZ
 */
public class SM_QUEST_ACTION extends AionServerPacket {

	private static final int NPC_FACTION_QUEST_OFFERED = 1;
	private static final int NPC_FACTION_QUEST_REMOVED = 2;

	private final ActionType actionType;
	private final int questId;
	private final int status;
	private final int progress;
	private final int value; // seconds for TIMER, sharer's object id for SHARE, offered or removed for NPC_FACTION_QUEST
	private final boolean flag; // shared within an alliance for SHARE

	private SM_QUEST_ACTION(ActionType actionType, int questId, int status, int progress, int value, boolean flag) {
		this.actionType = actionType;
		this.questId = questId;
		this.status = status;
		this.progress = progress;
		this.value = value;
		this.flag = flag;
	}

	/**
	 * Notifies the client about a new or abandoned quest, or a change of its state.
	 *
	 * @param actionType
	 *          {@link ActionType#ADD ADD}, {@link ActionType#UPDATE UPDATE} or {@link ActionType#ABANDON ABANDON}
	 */
	public SM_QUEST_ACTION(ActionType actionType, QuestState qs) {
		this(actionType, qs.getQuestId(), qs.getStatus().value(), qs.getQuestVars().getQuestVars(), 0, false);
	}

	/**
	 * Shows the quest timer, or removes it if the time is 0.
	 */
	public SM_QUEST_ACTION(int questId, int timeInSeconds) {
		this(ActionType.TIMER, questId, 0, 0, timeInSeconds, false);
	}

	/**
	 * Shows a question window that asks to share the quest with the packet receiver.
	 */
	public SM_QUEST_ACTION(int questId, int sharerId, boolean shareInAlliance) {
		this(ActionType.SHARE, questId, 0, 0, sharerId, shareInAlliance);
	}

	/**
	 * Adds the quest to the npc faction quests the client lists.
	 */
	public static SM_QUEST_ACTION npcFactionQuestOffered(int questId) {
		return new SM_QUEST_ACTION(ActionType.NPC_FACTION_QUEST, questId, 0, 0, NPC_FACTION_QUEST_OFFERED, false);
	}

	/**
	 * Removes the quest from the npc faction quests the client lists, when the player leaves the faction.
	 */
	public static SM_QUEST_ACTION npcFactionQuestRemoved(int questId) {
		return new SM_QUEST_ACTION(ActionType.NPC_FACTION_QUEST, questId, 0, 0, NPC_FACTION_QUEST_REMOVED, false);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(questId);
		if (questTemplate != null && questTemplate.getExtraCategory() != QuestExtraCategory.NONE)
			return;
		writeC(actionType.getId());
		writeD(questId);
		switch (actionType) {
			case ADD:
				writeH(status);
				writeD(progress); // all six quest vars
				writeH(0); // quest branch, which we don't have
				writeC(0); // 1 makes the client select the quest in its journal
				break;
			case UPDATE:
				writeH(status);
				writeD(progress); // all six quest vars
				writeH(0); // quest branch, which we don't have
				break;
			case ABANDON:
				writeD(0); // reason, 0 for giving up
				break;
			case TIMER:
				writeD(value);
				writeC(value > 0 ? 1 : 0);
				break;
			case SHARE:
				writeD(value);
				writeD(flag ? 1 : 0); // 0: group, 1: alliance
				break;
			case NPC_FACTION_QUEST:
				writeD(value);
				break;
		}
	}

	public enum ActionType {
		ADD(1),
		UPDATE(2),
		ABANDON(3),
		TIMER(4),
		SHARE(5),
		NPC_FACTION_QUEST(6);

		private final int id;

		ActionType(int id) {
			this.id = id;
		}

		int getId() {
			return id;
		}
	}
}
