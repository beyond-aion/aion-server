package com.aionemu.gameserver.questEngine.model;

import java.sql.Timestamp;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.templates.QuestTemplate;

/**
 * @author MrPoke
 * @modified vlog, Rolandas
 */
public class QuestState {

	private final int questId;
	private QuestVars questVars;
	private int questFlags;
	private QuestStatus status;
	private int completeCount;
	private Timestamp completeTime;
	private Timestamp nextRepeatTime;
	private Integer reward;
	private PersistentState persistentState;

	private static final Logger log = LoggerFactory.getLogger(QuestState.class);

	public QuestState(int questId, QuestStatus status, int questVars, int flags, int completeCount, Timestamp nextRepeatTime, Integer reward,
		Timestamp completeTime) {
		this.questId = questId;
		this.status = status;
		this.questVars = new QuestVars(questVars);
		this.questFlags = flags;
		this.completeCount = completeCount;
		this.nextRepeatTime = nextRepeatTime;
		this.reward = reward;
		this.completeTime = completeTime;
		this.persistentState = PersistentState.NEW;
	}

	public QuestState(int questId, QuestStatus status, int questVars, int completeCount, Timestamp nextRepeatTime, Integer reward,
		Timestamp completeTime) {
		this(questId, status, questVars, 0, completeCount, nextRepeatTime, reward, completeTime);
	}

	public QuestVars getQuestVars() {
		return questVars;
	}

	/**
	 * @param id
	 * @param var
	 */
	public void setQuestVarById(int id, int var) {
		questVars.setVarById(id, var);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @param id
	 * @return Quest var by id.
	 */
	public int getQuestVarById(int id) {
		return questVars.getVarById(id);
	}

	public void setQuestVar(int var) {
		questVars.setVar(var);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public QuestStatus getStatus() {
		return status;
	}

	public void setStatus(QuestStatus status) {
		if (status == QuestStatus.COMPLETE && this.status != QuestStatus.COMPLETE)
			updateCompleteTime();
		this.status = status;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public Timestamp getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Timestamp time) {
		completeTime = time;
	}

	public void updateCompleteTime() {
		completeTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
	}

	public int getQuestId() {
		return questId;
	}

	public void setCompleteCount(int completeCount) {
		this.completeCount = completeCount;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getCompleteCount() {
		return completeCount;
	}

	public void setNextRepeatTime(Timestamp nextRepeatTime) {
		this.nextRepeatTime = nextRepeatTime;
	}

	public Timestamp getNextRepeatTime() {
		return nextRepeatTime;
	}

	public void setReward(Integer reward) {
		this.reward = reward;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public Integer getReward() {
		if (reward == null) {
			log.warn("No reward for the quest " + String.valueOf(questId));
		} else {
			return reward;
		}
		return 0;
	}

	public boolean canRepeat() {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		if (status != QuestStatus.NONE
			&& (status != QuestStatus.COMPLETE || (completeCount >= template.getMaxRepeatCount() && template.getMaxRepeatCount() != 255))) {
			return false;
		}
		if (questVars.getQuestVars() != 0) {
			return false;
		}
		if (template.isTimeBased() && nextRepeatTime != null) {
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			if (currentTime.before(nextRepeatTime)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the pState
	 */
	public PersistentState getPersistentState() {
		return persistentState;
	}

	/**
	 * @param persistentState
	 *          the pState to set
	 */
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}

	/**
	 * Possibly it is the second set of quest vars, now are named as flags
	 * 
	 * @return the questFlags
	 */
	public int getFlags() {
		return questFlags;
	}

	/**
	 * Possibly it is the second set of quest vars, now are named as flags
	 * 
	 * @param questFlags
	 *          the questFlags to set
	 */
	public void setFlags(int questFlags) {
		this.questFlags = questFlags;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getStepGroup() {
		return questFlags >> 6;
	}

	public void setStepGroup(int groupNumber) {
		setFlags(groupNumber << 6);
	}
}
