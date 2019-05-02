package com.aionemu.gameserver.model.challenge;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.templates.challenge.ChallengeQuestTemplate;
import com.aionemu.gameserver.model.templates.challenge.ChallengeTaskTemplate;

/**
 * @author ViAl
 */
public class ChallengeTask {

	private final int taskId;
	private final int ownerId;
	private Map<Integer, ChallengeQuest> quests;
	private Timestamp completeTime;
	private ChallengeTaskTemplate template;

	/**
	 * Used for loading tasks from DAO.
	 * 
	 * @param header
	 * @param quests
	 * @param completeTime
	 */
	public ChallengeTask(int taskId, int ownerId, Map<Integer, ChallengeQuest> quests, Timestamp completeTime) {
		this.taskId = taskId;
		this.ownerId = ownerId;
		this.quests = quests;
		this.completeTime = completeTime;
		this.template = DataManager.CHALLENGE_DATA.getTaskByTaskId(taskId);
	}

	/**
	 * Used for creating new tasks in runtime.
	 * 
	 * @param ownerId
	 * @param template
	 */
	public ChallengeTask(int ownerId, ChallengeTaskTemplate template) {
		this.taskId = template.getId();
		this.ownerId = ownerId;
		Map<Integer, ChallengeQuest> quests = new HashMap<>();
		for (ChallengeQuestTemplate qt : template.getQuests()) {
			ChallengeQuest quest = new ChallengeQuest(qt, 0);
			quest.setPersistentState(PersistentState.NEW);
			quests.put(qt.getId(), quest);
		}
		this.quests = quests;
		this.template = template;
	}

	public int getTaskId() {
		return this.taskId;
	}

	public int getOwnerId() {
		return this.ownerId;
	}

	public int getQuestsCount() {
		return quests.size();
	}

	public Map<Integer, ChallengeQuest> getQuests() {
		return quests;
	}

	public ChallengeQuest getQuest(int questId) {
		return quests.get(questId);
	}

	public Timestamp getCompleteTime() {
		return completeTime;
	}

	public int getCompleteTimeEpochSeconds() {
		return completeTime == null ? 0 : (int) (completeTime.getTime() / 1000);
	}

	public synchronized void updateCompleteTime() {
		completeTime = new Timestamp(System.currentTimeMillis());
	}

	public ChallengeTaskTemplate getTemplate() {
		return this.template;
	}

	public boolean isCompleted() {
		boolean isCompleted = true;
		for (ChallengeQuest quest : quests.values()) {
			if (quest.getCompleteCount() < quest.getMaxRepeats()) {
				isCompleted = false;
				break;
			}
		}
		return isCompleted;
	}
}
