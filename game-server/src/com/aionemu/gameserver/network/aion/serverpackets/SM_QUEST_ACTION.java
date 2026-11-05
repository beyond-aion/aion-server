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
	private static final int INSTANCE_TIMER_ID_BIT = 1 << 31; // keeps instance timer ids apart from quest ids
	public static final int INSTANCE_TIMER_STYLE = 5;

	private final ActionType actionType;
	private final int questId;
	private final int status;
	private final int progress;
	private final int value; // seconds for TIMER, sharer's object id for SHARE, offered or removed for NPC_FACTION_QUEST
	private final boolean flag; // select in the journal for ADD, shared within an alliance for SHARE
	private final int timerStyle;

	private SM_QUEST_ACTION(ActionType actionType, int questId, int status, int progress, int value, boolean flag) {
		this(actionType, questId, status, progress, value, flag, 0);
	}

	private SM_QUEST_ACTION(ActionType actionType, int questId, int status, int progress, int value, boolean flag, int timerStyle) {
		this.actionType = actionType;
		this.questId = questId;
		this.status = status;
		this.progress = progress;
		this.value = value;
		this.flag = flag;
		this.timerStyle = timerStyle;
	}

	/**
	 * Notifies the client about a new or abandoned quest, or a change of its state.
	 *
	 * @param actionType
	 *          {@link ActionType#ADD ADD}, {@link ActionType#UPDATE UPDATE} or {@link ActionType#ABANDON ABANDON}
	 */
	public SM_QUEST_ACTION(ActionType actionType, QuestState qs) {
		this(actionType, qs, false);
	}

	/**
	 * @param selectInJournal
	 *          for {@link ActionType#ADD ADD}: whether the client selects the new quest in its journal, as it does for missions the server hands
	 *          out by itself
	 */
	public SM_QUEST_ACTION(ActionType actionType, QuestState qs, boolean selectInJournal) {
		this(actionType, qs.getQuestId(), qs.getStatus().value(), qs.getQuestVars().getQuestVars(), 0, selectInJournal);
	}

	/**
	 * Shows the quest timer, or removes it if the time is 0.
	 */
	public SM_QUEST_ACTION(int questId, int timeInSeconds) {
		this(ActionType.TIMER, questId, 0, 0, timeInSeconds, false, timeInSeconds > 0 ? 1 : 0);
	}

	/**
	 * Shows the time limit of an instance, or removes it if the time is 0. The client shows one timer at a time and only accepts changes to the
	 * one it shows, so removing it needs the same instance id.
	 */
	public static SM_QUEST_ACTION instanceTimer(int instanceId, int timeInSeconds) {
		return instanceTimer(instanceId, timeInSeconds, INSTANCE_TIMER_STYLE);
	}

	/**
	 * @param style
	 *          how the client draws the timer, {@link #INSTANCE_TIMER_STYLE} for most instances
	 */
	public static SM_QUEST_ACTION instanceTimer(int instanceId, int timeInSeconds, int style) {
		return new SM_QUEST_ACTION(ActionType.TIMER, INSTANCE_TIMER_ID_BIT | instanceId, 0, 0, timeInSeconds, false, style);
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
				writeC(flag ? 1 : 0); // select the quest in the journal
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
				writeC(timerStyle);
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
