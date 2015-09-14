package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestExtraCategory;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class SM_QUEST_ACTION extends AionServerPacket {

	protected int questId;
	private int status;
	private int step;
	private int flags;
	protected int action;
	private int timer;
	private int sharerId;
	@SuppressWarnings("unused")
	private boolean unk;

	SM_QUEST_ACTION() {

	}

	/**
	 * Accept Quest(1)
	 * 
	 * @param questId
	 * @param status
	 * @param step
	 */
	public SM_QUEST_ACTION(int questId, int status, int step, int flags) {
		this.action = 1;
		this.questId = questId;
		this.status = status;
		this.step = step;
		this.flags = flags;
	}

	/**
	 * Quest Steps/Finish (2)
	 * 
	 * @param questId
	 * @param status
	 * @param step
	 */
	public SM_QUEST_ACTION(int questId, QuestStatus status, int step, int flags) {
		this.action = 2;
		this.questId = questId;
		this.status = status.value();
		this.step = step;
		this.flags = flags;
	}

	/**
	 * Delete Quest(3)
	 * 
	 * @param questId
	 */
	public SM_QUEST_ACTION(int questId) {
		this.action = 3;
		this.questId = questId;
	}

	/**
	 * Display Timer(4)
	 * 
	 * @param questId
	 * @param timer
	 */
	public SM_QUEST_ACTION(int questId, int timer) {
		this.action = 4;
		this.questId = questId;
		this.timer = timer;
		this.step = 0;
	}

	public SM_QUEST_ACTION(int questId, int sharerId, boolean unk) {
		this.action = 5;
		this.questId = questId;
		this.sharerId = sharerId;
		this.unk = unk;
	}

	/**
	 * Display Timer(4)
	 * 
	 * @param questId
	 * @param timer
	 */
	public SM_QUEST_ACTION(int questId, boolean fake) {
		this.action = 6;
		this.questId = questId;
		this.timer = 0;
		this.step = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.commons.network.mmocore.SendablePacket#writeImpl(com.aionemu.commons.network.mmocore.MMOConnection)
	 */
	@Override
	protected void writeImpl(AionConnection con) {

		QuestTemplate questTemplate = DataManager.QUEST_DATA.getQuestById(questId);
		if (questTemplate != null && questTemplate.getExtraCategory() != QuestExtraCategory.NONE)
			return;
		writeC(action);
		writeD(questId);
		switch (action) {
			case 1:
				writeC(status);// quest status goes by ENUM value
				writeC(0x0);
				writeD(step | flags << 24);// current quest step
				writeH(0);
				writeC(0);
				break;
			case 2:
				writeC(status);// quest status goes by ENUM value
				writeC(0x0);
				writeD(step | flags << 24);// current quest step
				writeH(0);
				break;
			case 3:
				writeD(0);
				break;
			case 4:
				writeD(timer);// sets client timer ie 84030000 is 900 seconds/15 mins
				writeC(timer > 0 ? 1 : 0);
			case 5:
				writeD(this.sharerId);
				writeD(0);
				break;
			case 6:
				writeH(0x01);// ???
				writeH(0x0);
		}
	}
}
