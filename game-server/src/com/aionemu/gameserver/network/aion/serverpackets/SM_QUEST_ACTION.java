package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestExtraCategory;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author VladimirZ
 */
public class SM_QUEST_ACTION extends AionServerPacket {

	private ActionType actionType;
	private int questId;
	private int status;
	private int step;
	private int flags;
	private int timer;
	private int sharerId;
	private boolean shareInAlliance;

	/**
	 * Use this constructor to notify the client about a new/abandoned quest or quest state updates.
	 * 
	 * @param actionType
	 *          - {@link ActionType#ADD ADD}, {@link ActionType#UPDATE UPDATE} or {@link ActionType#ABANDON ABANDON}
	 * @param qs
	 *          - concerned quest
	 */
	public SM_QUEST_ACTION(ActionType actionType, QuestState qs) {
		this.actionType = actionType;
		this.questId = qs.getQuestId();
		this.status = qs.getStatus().value();
		this.step = qs.getQuestVars().getQuestVars();
		this.flags = qs.getFlags();
	}

	public SM_QUEST_ACTION(int questId) {
		this.actionType = ActionType.UNK;
		this.questId = questId;
	}

	/**
	 * Display Timer
	 * 
	 * @param questId
	 * @param timer
	 */
	public SM_QUEST_ACTION(int questId, int timer) {
		this.actionType = ActionType.TIMER;
		this.questId = questId;
		this.timer = timer;
	}

	/**
	 * Shows a question window that asks to share the quest with the packet receiver.
	 */
	public SM_QUEST_ACTION(int questId, int sharerId, boolean shareInAlliance) {
		this.actionType = ActionType.SHARE;
		this.questId = questId;
		this.sharerId = sharerId;
		this.shareInAlliance = shareInAlliance;
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
				writeC(status);// quest status goes by ENUM value
				writeC(0x0);
				writeD(step | flags << 24);// current quest step
				writeH(0);
				writeC(0); // seen sometimes 1 for campaign quests
				break;
			case UPDATE:
				writeC(status);// quest status goes by ENUM value
				writeC(0x0);
				writeD(step | flags << 24);// current quest step
				writeH(0); // seen sometimes 1 when status == COMPLETED
				break;
			case ABANDON:
				writeD(0);
				break;
			case TIMER:
				writeD(timer);// sets client timer ie 84030000 is 900 seconds/15 mins
				writeC(timer > 0 ? 1 : 0);
				break;
			case SHARE:
				writeD(sharerId);
				writeD(shareInAlliance ? 1 : 0); // 0: group, 1: alliance
				break;
			case UNK:
				writeH(0x01);// ???
				writeH(0x0);
				break;
		}
	}

	public enum ActionType {
		ADD(1),
		UPDATE(2),
		ABANDON(3),
		TIMER(4),
		SHARE(5),
		UNK(6);

		private final int id;

		ActionType(int id) {
			this.id = id;
		}

		int getId() {
			return id;
		}
	}
}
