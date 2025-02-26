package com.aionemu.gameserver.model.gameobjects.player;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke, vlog, Neon
 */
public class QuestStateList {

	private static final Logger log = LoggerFactory.getLogger(QuestStateList.class);
	private SortedMap<Integer, QuestState> quests = new TreeMap<>();
	private Set<Integer> deletedQuests = new HashSet<>();

	/**
	 * Creates an empty quests list
	 */
	public QuestStateList() {
	}

	/**
	 * @param questId
	 * @return True if there is a quest in the list with this id.
	 */
	public boolean hasQuest(int questId) {
		return quests.containsKey(questId);
	}

	public synchronized boolean addQuest(int questId, QuestState questState) {
		if (quests.containsKey(questId)) {
			log.warn("Tried to add duplicate quest to quest list: " + questId);
			return false;
		}
		quests.put(questId, questState);
		return true;
	}

	/**
	 * @param questId
	 * @return The quest that was deleted, null if it didn't exist in the list.
	 */
	public synchronized QuestState deleteQuest(int questId) {
		QuestState qs = quests.remove(questId);
		if (qs != null) {
			deletedQuests.add(qs.getQuestId());
			qs.setPersistentState(PersistentState.DELETED);
		}
		return qs;
	}

	public QuestState getQuestState(int questId) {
		return quests.get(questId);
	}

	/**
	 * @return All quests, including abandoned ones since login.
	 */
	public List<QuestState> getAllQuestState() {
		return new ArrayList<>(quests.values());
	}

	/**
	 * @return All quests, that have been completed at least once.
	 */
	public List<QuestState> getCompletedQuests() {
		return quests.values().stream().filter(qs -> qs.getCompleteCount() > 0).collect(Collectors.toList());
	}

	/**
	 * @return All quests, that are currently active or locked.
	 */
	public List<QuestState> getUncompletedQuests() {
		return quests.values().stream().filter(qs -> qs.getStatus() != QuestStatus.COMPLETE).collect(Collectors.toList());
	}

	/**
	 * @return All normal (light blue) quests, that are currently active.
	 */
	public List<QuestState> getNormalQuests() {
		List<QuestState> questList = new ArrayList<>();
		for (QuestState qs : getAllQuestState()) {
			QuestCategory qc = DataManager.QUEST_DATA.getQuestById(qs.getQuestId()).getCategory();
			QuestStatus s = qs.getStatus();

			if (qc == QuestCategory.QUEST && s != QuestStatus.COMPLETE && s != QuestStatus.LOCKED) {
				questList.add(qs);
			}
		}
		return questList;
	}

	/**
	 * @return IDs of all quests that are specifically marked as deleted (this set will be cleared after each DB update).
	 */
	public Set<Integer> getDeletedQuestIds() {
		return deletedQuests;
	}
}
