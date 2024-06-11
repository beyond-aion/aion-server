package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.challenge.ChallengeQuest;
import com.aionemu.gameserver.model.challenge.ChallengeTask;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.templates.challenge.ChallengeQuestTemplate;
import com.aionemu.gameserver.model.templates.challenge.ChallengeType;

/**
 * @author ViAl
 */
public class ChallengeTasksDAO {

	private static final Logger log = LoggerFactory.getLogger(ChallengeTasksDAO.class);

	private static final String SELECT_QUERY = "SELECT * FROM `challenge_tasks` WHERE `owner_id` = ? AND `owner_type` = ?";
	private static final String INSERT_QUERY = "INSERT INTO `challenge_tasks` (`task_id`, `quest_id`, `owner_id`, `owner_type`, `complete_count`, `complete_time`) VALUES (?, ?, ?, ?, ?, ?);";
	private static final String UPDATE_QUERY = "UPDATE `challenge_tasks` SET `complete_count` = ?, `complete_time`= ? WHERE `task_id` = ? AND `quest_id` = ? AND `owner_id` = ?";

	public static Map<Integer, ChallengeTask> load(int ownerId, ChallengeType type) {
		ConcurrentHashMap<Integer, ChallengeTask> tasks = new ConcurrentHashMap<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, ownerId);
			stmt.setString(2, type.toString());
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int taskId = rset.getInt("task_id");
					int questId = rset.getInt("quest_id");
					int completeCount = rset.getInt("complete_count");
					Timestamp date = rset.getTimestamp("complete_time");
					ChallengeQuestTemplate template = DataManager.CHALLENGE_DATA.getQuestByQuestId(questId);
					ChallengeQuest quest = new ChallengeQuest(template, completeCount);
					quest.setPersistentState(PersistentState.UPDATED);
					if (!tasks.containsKey(taskId)) {
						Map<Integer, ChallengeQuest> quests = new HashMap<>(2);
						quests.put(quest.getQuestId(), quest);
						ChallengeTask task = new ChallengeTask(taskId, ownerId, quests, date);
						tasks.putIfAbsent(taskId, task);
					} else {
						tasks.get(taskId).getQuests().put(questId, quest);
					}
				}
			}
		} catch (SQLException e) {
			log.error("Could not load " + type + " challenge tasks of owner " + ownerId, e);
		}
		return tasks;
	}

	public static void storeTask(ChallengeTask task) {
		for (ChallengeQuest quest : task.getQuests().values()) {
			switch (quest.getPersistentState()) {
				case NEW -> insertQuestEntry(task, quest);
				case UPDATE_REQUIRED -> updateQuestEntry(task, quest);
			}
		}
	}

	private static void insertQuestEntry(ChallengeTask task, ChallengeQuest quest) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, task.getTaskId());
			stmt.setInt(2, quest.getQuestId());
			stmt.setInt(3, task.getOwnerId());
			stmt.setString(4, task.getTemplate().getType().toString());
			stmt.setInt(5, quest.getCompleteCount());
			stmt.setTimestamp(6, task.getCompleteTime());
			stmt.executeUpdate();
			quest.setPersistentState(PersistentState.UPDATED);
		} catch (SQLException e) {
			log.error("Could not insert challenge task " + task.getTaskId() + " of owner " + task.getOwnerId(), e);
		}
	}

	private static void updateQuestEntry(ChallengeTask task, ChallengeQuest quest) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, quest.getCompleteCount());
			stmt.setTimestamp(2, task.getCompleteTime());
			stmt.setInt(3, task.getTaskId());
			stmt.setInt(4, quest.getQuestId());
			stmt.setInt(5, task.getOwnerId());
			stmt.executeUpdate();
			quest.setPersistentState(PersistentState.UPDATED);
		} catch (SQLException e) {
			log.error("Could not update challenge task " + task.getTaskId() + " of owner " + task.getOwnerId(), e);
		}
	}

}
