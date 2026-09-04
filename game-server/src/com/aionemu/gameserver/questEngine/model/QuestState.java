package com.aionemu.gameserver.questEngine.model;

import java.sql.Timestamp;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.templates.QuestTemplate;

/**
 * @author MrPoke, vlog, Rolandas
 */
public class QuestState implements Persistable {

	/** The flags hold a value in their low bits and the step group above it */
	private static final int STEP_GROUP_SHIFT = 6;
	private static final int FLAG_VALUE_MASK = (1 << STEP_GROUP_SHIFT) - 1;

	private int questId;
	private QuestVars questVars;
	private int questFlags;
	private QuestStatus status;
	private int completeCount;
	private Timestamp completeTime;
	private Timestamp nextRepeatTime;
	private Integer reward;
	private PersistentState persistentState;

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

	public QuestState(int questId, QuestStatus status) {
		this(questId, status, 0, 0, status == QuestStatus.COMPLETE ? 1 : 0, null, null,
			status == QuestStatus.COMPLETE ? new Timestamp(System.currentTimeMillis()) : null);
	}

	public QuestVars getQuestVars() {
		return questVars;
	}

	public void setQuestVarById(int id, int var) {
		questVars.setVarById(id, var);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

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
		setStatus(status, true);
	}

	public void setStatus(QuestStatus status, boolean updateCompleteCountAndTime) {
		if (status == QuestStatus.COMPLETE && this.status != QuestStatus.COMPLETE && updateCompleteCountAndTime) {
			completeTime = new Timestamp(System.currentTimeMillis());
			completeCount++;
		}
		this.status = status;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public Timestamp getLastCompleteTime() {
		return completeTime;
	}

	public int getQuestId() {
		return questId;
	}

	public int getCompleteCount() {
		return completeCount;
	}

	public void setCompleteCount(int completeCount) {
		this.completeCount = completeCount;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public void setNextRepeatTime(Timestamp nextRepeatTime) {
		this.nextRepeatTime = nextRepeatTime;
	}

	public Timestamp getNextRepeatTime() {
		return nextRepeatTime;
	}

	public void setRewardGroup(Integer reward) {
		this.reward = reward;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return The reward group or null if not set
	 */
	public Integer getRewardGroup() {
		return reward;
	}

	/**
	 * @return True, if the quest is not active or is complete and can currently be repeated.
	 */
	public boolean isStartable() {
		return status == QuestStatus.COMPLETE && canRepeat();
	}

	public boolean canRepeat() {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		if (completeCount >= template.getMaxRepeatCount() && template.getMaxRepeatCount() != 255)
			return false;
		if (template.isTimeBased() && nextRepeatTime != null) {
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			if (currentTime.before(nextRepeatTime))
				return false;
		}
		return true;
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else
					this.persistentState = PersistentState.DELETED;
				break;
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

	/** @return The value stored in the flag bits, without the step group above them. */
	public int getFlagValue() {
		return questFlags & FLAG_VALUE_MASK;
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
		return questFlags >> STEP_GROUP_SHIFT;
	}

	public void setStepGroup(int groupNumber) {
		setFlags(groupNumber << STEP_GROUP_SHIFT);
	}
}
