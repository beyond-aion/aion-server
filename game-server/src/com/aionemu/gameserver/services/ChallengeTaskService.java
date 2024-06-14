package com.aionemu.gameserver.services;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dao.ChallengeTasksDAO;
import com.aionemu.gameserver.dao.TownDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.challenge.ChallengeQuest;
import com.aionemu.gameserver.model.challenge.ChallengeTask;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.templates.challenge.ChallengeTaskTemplate;
import com.aionemu.gameserver.model.templates.challenge.ChallengeType;
import com.aionemu.gameserver.model.templates.challenge.ContributionReward;
import com.aionemu.gameserver.model.town.Town;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHALLENGE_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl
 */
public class ChallengeTaskService {

	private static final Logger log = LoggerFactory.getLogger(ChallengeTaskService.class);
	private final Map<Integer, Map<Integer, Integer>> taskAcceptTownIds;
	private final Map<Integer, Map<Integer, ChallengeTask>> cityTasks;
	private final Map<Integer, Map<Integer, ChallengeTask>> legionTasks;

	private static class SingletonHolder {

		protected static final ChallengeTaskService instance = new ChallengeTaskService();
	}

	public static ChallengeTaskService getInstance() {
		return SingletonHolder.instance;
	}

	private ChallengeTaskService() {
		taskAcceptTownIds = new ConcurrentHashMap<>();
		cityTasks = new ConcurrentHashMap<>();
		legionTasks = new ConcurrentHashMap<>();
		log.info("ChallengeTaskService initialized.");
	}

	public void showTaskList(Player player, ChallengeType challengeType, int ownerId) {
		if (CustomConfig.CHALLENGE_TASKS_ENABLED) {
			int ownerLevel = 0;
			switch (challengeType) {
				case TOWN:
					ownerLevel = TownService.getInstance().getTownById(ownerId).getLevel();
					break;
				case LEGION:
					ownerLevel = player.getLegion().getLegionLevel();
					break;
			}
			List<ChallengeTask> availableTasks = buildTaskList(player, challengeType, ownerId, ownerLevel);
			PacketSendUtility.sendPacket(player, new SM_CHALLENGE_LIST(2, ownerId, challengeType, availableTasks));
			for (ChallengeTask task : availableTasks) {
				PacketSendUtility.sendPacket(player, new SM_CHALLENGE_LIST(7, ownerId, challengeType, task));
			}
		}
	}

	private List<ChallengeTask> buildTaskList(Player player, ChallengeType challengeType, int ownerId, int ownerLevel) {
		Map<Integer, Map<Integer, ChallengeTask>> taskMap;
		if (challengeType == ChallengeType.LEGION)
			taskMap = legionTasks;
		else
			taskMap = cityTasks;
		int playerTownId = TownService.getInstance().getTownResidence(player);
		List<ChallengeTask> availableTasks = new ArrayList<>();
		if (!taskMap.containsKey(ownerId)) {
			Map<Integer, ChallengeTask> tasks = ChallengeTasksDAO.load(ownerId, challengeType);
			taskMap.put(ownerId, tasks);
		}
		for (ChallengeTask ct : taskMap.get(ownerId).values()) {
			if (ct.getTemplate().isRepeatable() || !ct.isCompleted())
				availableTasks.add(ct);
		}
		for (ChallengeTaskTemplate template : DataManager.CHALLENGE_DATA.getTasks().values()) {
			if (template.getType() == challengeType && template.getRace() == player.getRace()) {
				if (!taskMap.get(ownerId).containsKey(template.getId())) {
					if (ownerLevel >= template.getMinLevel() && ownerLevel <= template.getMaxLevel()) {
						if (template.isTownResidence() && playerTownId != ownerId) {
							continue;
						}
						if (template.getPrevTask() == null) {
							ChallengeTask task = new ChallengeTask(ownerId, template);
							taskMap.get(ownerId).put(task.getTaskId(), task);
							ChallengeTasksDAO.storeTask(task);
							availableTasks.add(task);
						} else {
							int prevTaskId = template.getPrevTask();
							if (taskMap.get(ownerId).containsKey(prevTaskId)) {
								ChallengeTask prevTask = taskMap.get(ownerId).get(prevTaskId);
								if (prevTask.isCompleted()) {
									ChallengeTask task = new ChallengeTask(ownerId, template);
									taskMap.get(ownerId).put(task.getTaskId(), task);
									ChallengeTasksDAO.storeTask(task);
									availableTasks.add(task);
								}
							}
						}
					}
				}
			}
		}
		return availableTasks;
	}

	public void onChallengeQuestFinish(Player player, int questId) {
		ChallengeTaskTemplate taskTemplate = DataManager.CHALLENGE_DATA.getTaskByQuestId(questId);
		switch (taskTemplate.getType()) {
			case TOWN:
				onCityTaskFinish(player, taskTemplate, questId);
				break;
			case LEGION:
				onLegionTaskFinish(player, taskTemplate, questId);
				break;
		}
	}

	public void onAcceptTask(Player player, int questId) {
		int townId = TownService.getInstance().getTownIdByPosition(player);
		if (townId != 0)
			taskAcceptTownIds.computeIfAbsent(player.getObjectId(), k -> new HashMap<>()).put(questId, townId);
	}

	private void onCityTaskFinish(Player player, ChallengeTaskTemplate taskTemplate, int questId) {
		Map<Integer, Integer> townsByQuestId = taskAcceptTownIds.get(player.getObjectId());
		Integer townId = townsByQuestId == null ? null : townsByQuestId.remove(questId);
		if (townId == null) // server got restarted after player accepted the quest or quest got started outside town (by chat command)
			return;
		ChallengeTask task = getChallengeTask(player, taskTemplate, townId);
		if (task == null) // task may be not available anymore due to town levelup
			return;
		ChallengeQuest quest = task.getQuest(questId);
		if (quest == null) {
			log.warn(player + " finished city task " + task.getTaskId() + " of town " + townId + " but info for quest " + questId + " is missing.");
			return;
		}
		if (quest.getCompleteCount() < quest.getMaxRepeats() && !task.isCompleted()) {
			task.updateCompleteTime();
			quest.increaseCompleteCount();
			ChallengeTasksDAO.storeTask(task);
			Town town = TownService.getInstance().getTownById(townId);
			if (town != null) {
				int oldLevel = town.getLevel();
				town.increasePoints(quest.getScorePerQuest());
				if (task.isCompleted()) {
					switch (taskTemplate.getReward().getType()) {
						case POINT:
							town.increasePoints(taskTemplate.getReward().getValue());
							break;
						case SPAWN:
							// TODO
							break;
					}
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_TOWN_MISSION_COMPLETE(town.getL10n(), task.getTemplate().getL10n()));
				}
				if (town.getLevel() != oldLevel)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_TOWN_LEVEL_LEVEL_UP(town.getL10n(), town.getLevel()));
				TownDAO.store(town);
			}
		}
	}

	private ChallengeTask getChallengeTask(Player player, ChallengeTaskTemplate taskTemplate, int townId) {
		Map<Integer, ChallengeTask> taskMap = cityTasks.get(townId);
		if (taskMap == null) {
			buildTaskList(player, ChallengeType.TOWN, townId, TownService.getInstance().getTownById(townId).getLevel());
			taskMap = cityTasks.get(townId);
			if (taskMap == null) {
				log.warn("Town " + townId + " has no CityTasks! " + player + ", town residence:" + TownService.getInstance().getTownResidence(player));
				return null;
			}
		}
		return taskMap.get(taskTemplate.getId());
	}

	private void onLegionTaskFinish(Player player, ChallengeTaskTemplate taskTemplate, int questId) {
		// Player could take challenge task and after that leave legion.
		if (player.getLegion() == null)
			return;
		int legionId = player.getLegion().getLegionId();
		// If player took challenge task in one legion, then leave that legion and enter another.
		if (!legionTasks.containsKey(legionId))
			return;
		// If player took challenge task in one legion, then leave that legion and enter another, and after that completed this task in new legion.
		if (legionTasks.get(legionId).get(taskTemplate.getId()) == null)
			return;
		ChallengeTask task = legionTasks.get(player.getLegion().getLegionId()).get(taskTemplate.getId());
		ChallengeQuest quest = task.getQuests().get(questId);
		if (quest.getCompleteCount() >= quest.getMaxRepeats())
			return;
		player.getLegionMember().increaseChallengeScore(quest.getScorePerQuest());
		if (!task.isCompleted()) {
			task.updateCompleteTime();
			quest.increaseCompleteCount();
			player.getLegion().getOnlineLegionMembers().forEach(p -> showTaskList(p, ChallengeType.LEGION, legionId));
			ChallengeTasksDAO.storeTask(task);
			if (task.isCompleted()) {
				TreeMap<Integer, List<Integer>> winnersByPoints = new TreeMap<>();
				for (Integer memberObjId : player.getLegion().getLegionMembers()) {
					LegionMember legionMember = LegionService.getInstance().getLegionMember(memberObjId);
					winnersByPoints.computeIfAbsent(legionMember.getChallengeScore(), k -> new ArrayList<>()).add(memberObjId);
					legionMember.setChallengeScore(0);
					if (World.getInstance().getPlayer(memberObjId) == null) // save legionMember to DB since owning player is not online (no autosave schedule)
						LegionService.getInstance().storeLegionMember(legionMember);
				}
				int rewardsAdded = 0, itemId, itemCount;
				for (Entry<Integer, List<Integer>> e : winnersByPoints.descendingMap().entrySet()) {
					for (int objectId : e.getValue()) {
						for (ContributionReward reward : taskTemplate.getContrib()) {
							if (rewardsAdded <= reward.getNumber()) {
								rewardsAdded++;
								itemId = reward.getRewardId();
								itemCount = reward.getItemCount();
								String recipientName = PlayerService.getPlayerName(objectId);
								SystemMailService.sendMail("Legion reward", recipientName, "", "", itemId, itemCount, 0, LetterType.NORMAL);
								break;
							}
						}
					}
					e.getValue().clear();
				}
			}
		}
	}

	public boolean canRaiseLegionLevel(Legion legion, Player actingPlayer) {
		Map<Integer, ChallengeTask> tasks = legionTasks.computeIfAbsent(legion.getLegionId(),
			id -> ChallengeTasksDAO.load(id, ChallengeType.LEGION));
		List<ChallengeTask> requiredTasksForLevel = new ArrayList<>();
		for (ChallengeTask task : tasks.values()) {
			ChallengeTaskTemplate taskTemplate = task.getTemplate();
			if (taskTemplate.isLegionLevelTask() && taskTemplate.getMinLevel() == legion.getLegionLevel())
				requiredTasksForLevel.add(task);
		}
		if (requiredTasksForLevel.isEmpty()) {
			log.warn("{} tried to increase level of {} but no challenge tasks were found", actingPlayer, legion);
			return false;
		}
		for (ChallengeTask task : requiredTasksForLevel)
			if (!task.isCompleted())
				return false;
		return true;
	}
}
