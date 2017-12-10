package com.aionemu.gameserver.model.challenge;

import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.templates.challenge.ChallengeQuestTemplate;

/**
 * @author ViAl
 */
public class ChallengeQuest implements Persistable {

	private final ChallengeQuestTemplate template;
	private int completeCount;
	private PersistentState persistentState;

	/**
	 * @param template
	 * @param completeCount
	 */
	public ChallengeQuest(ChallengeQuestTemplate template, int completeCount) {
		this.template = template;
		this.completeCount = completeCount;
	}

	public int getQuestId() {
		return template.getId();
	}

	public ChallengeQuestTemplate getQuestTemplate() {
		return template;
	}

	public int getMaxRepeats() {
		return template.getRepeatCount();
	}

	public int getScorePerQuest() {
		return template.getScore();
	}

	public int getCompleteCount() {
		return completeCount;
	}

	public synchronized void increaseCompleteCount() {
		this.completeCount++;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public void setPersistentState(PersistentState persistentState) {
		if (this.persistentState == PersistentState.NEW && persistentState == PersistentState.UPDATE_REQUIRED)
			return;
		this.persistentState = persistentState;
	}
}
