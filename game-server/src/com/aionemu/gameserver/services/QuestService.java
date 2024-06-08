package com.aionemu.gameserver.services;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.quest.*;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS.Status;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.models.WorkOrdersData;
import com.aionemu.gameserver.questEngine.handlers.models.XMLQuest;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.reward.BonusService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Mr. Poke, vlog, bobobear, xTz, Rolandas
 */
public final class QuestService {

	private static final Logger log = LoggerFactory.getLogger(QuestService.class);
	private static Map<Integer, List<QuestDrop>> questDrop = new HashMap<>();

	/**
	 * Finishes the quest and rewards the player.
	 */
	public static boolean finishQuest(QuestEnv env) {
		Player player = env.getPlayer();
		int id = env.getQuestId();
		QuestState qs = player.getQuestStateList().getQuestState(id);

		Rewards rewards = new Rewards();
		Rewards extendedRewards = new Rewards();
		if (qs == null || qs.getStatus() != QuestStatus.REWARD)
			return false;
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
		if (template.getCategory() == QuestCategory.MISSION && qs.getCompleteCount() != 0)
			return false; // prevent repeatable reward because of wrong quest handling

		validateAndFixRewardGroup(qs, id);
		List<QuestItems> questItems = new ArrayList<>();
		if (template.getExtendedRewards() != null && qs.getCompleteCount() == template.getRewardRepeatCount() - 1) { // additional reward for the Xth time
			questItems.addAll(getRewardItems(env, template, true, null));
			extendedRewards = template.getExtendedRewards();
		}
		if (!template.getRewards().isEmpty() || template.getBonus() != null) {
			questItems.addAll(getRewardItems(env, template, false, qs.getRewardGroup()));
			if (qs.getRewardGroup() != null)
				rewards = template.getRewards().get(qs.getRewardGroup());
		}
		for (QuestItems qi : questItems)
			ItemService.addItem(player, qi.getItemId(), qi.getCount(), true);
		giveReward(env, rewards);
		giveReward(env, extendedRewards);
		if (template.getCategory() == QuestCategory.CHALLENGE_TASK)
			ChallengeTaskService.getInstance().onChallengeQuestFinish(player, id);
		removeQuestWorkItems(player, qs); // remove all worker list item if finished
		qs.setStatus(QuestStatus.COMPLETE);
		qs.setQuestVar(0);
		if (template.isTimeBased())
			qs.setNextRepeatTime(calculateRepeatDate(player, template));
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.UPDATE, qs));
		QuestEngine.getInstance().onQuestCompleted(player, id);
		if (template.getNpcFactionId() != 0)
			player.getNpcFactions().completeQuest(template);
		player.getController().updateNearbyQuests();
		return true;
	}

	/**
	 * Validates and sets/corrects (if necessary) the reward group which is to be used. Must only be called in reward state.
	 */
	public static void validateAndFixRewardGroup(QuestState qs, int questId) {
		if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			List<Rewards> rewardGroups = DataManager.QUEST_DATA.getQuestById(questId).getRewards();
			if (qs.getRewardGroup() != null) {
				if (rewardGroups == null) {
					log.warn("Handler for quest " + questId + " has set a reward group, but there are none in quest_data.xml.");
					qs.setRewardGroup(null);
				} else if (qs.getRewardGroup() < 0 || qs.getRewardGroup() >= rewardGroups.size()) {
					log.warn("Handler for quest " + questId + " tried to reward a nonexistent reward group (index " + qs.getRewardGroup() + ").");
					qs.setRewardGroup(rewardGroups.size() - 1);
				}
			} else { // you must explicitly specify the reward group when there are more than 1
				if (rewardGroups != null && rewardGroups.size() > 0) {
					if (rewardGroups.size() > 1)
						log.warn("Handler for quest " + questId + " possibly rewarded the wrong reward group.");
					qs.setRewardGroup(0);
				}
			}
		}
	}

	private static List<QuestItems> getRewardItems(QuestEnv env, QuestTemplate template, boolean extended, Integer rewardGroup) {
		Player player = env.getPlayer();
		int id = env.getQuestId();
		int dialogActionId = env.getDialogActionId();
		List<QuestItems> questItems = new ArrayList<>();
		if (extended) {
			Rewards rewards = template.getExtendedRewards();
			questItems.addAll(rewards.getRewardItem());
			if (dialogActionId == SELECTED_QUEST_NOREWARD && !rewards.getSelectableRewardItem().isEmpty()) {
				int index = env.getExtendedRewardIndex();
				if (index - 8 >= 0 && index - 8 < rewards.getSelectableRewardItem().size()) {
					questItems.add(rewards.getSelectableRewardItem().get(index - 8));
				} else if ((index - 1) >= 0 && (index - 1) < rewards.getSelectableRewardItem().size()) {
					questItems.add(rewards.getSelectableRewardItem().get(index - 1));
				} else {
					log.warn("The extended SelectableRewardItem list has no element on index " + (index - 8) + ". See quest id " + env.getQuestId()
						+ ". The size is: " + rewards.getSelectableRewardItem().size());
				}
			}
		} else {
			if (rewardGroup != null) {
				Rewards rewards = template.getRewards().get(rewardGroup);
				questItems.addAll(rewards.getRewardItem());
				QuestState qs = player.getQuestStateList().getQuestState(id);
				PlayerClass playerClass = player.getCommonData().getPlayerClass();
				int rewardIndex = getRewardIndex(env.getDialogActionId());
				if (rewardIndex >= 0) {
					boolean isLastRepeat = qs.getCompleteCount() == template.getRewardRepeatCount() - 1;
					if (isLastRepeat && template.isSingleTimeClassReward() || template.isClassRewardOnEveryRepeat()) {
						if (rewardIndex < template.getSelectableRewardByClass(playerClass).size()) {
							questItems.add(template.getSelectableRewardByClass(playerClass).get(rewardIndex));
						} else {
							log.warn("The SelectableRewardByClass list has no element on index " + rewardIndex + ". See quest id " + env.getQuestId()
								+ ". The size for " + playerClass + " is: " + template.getSelectableRewardByClass(playerClass).size());
						}
					} else if (rewardIndex < rewards.getSelectableRewardItem().size()) {
						questItems.add(rewards.getSelectableRewardItem().get(rewardIndex));
					} else {
						log.warn("The SelectableRewardItem list has no element on index " + rewardIndex + ". See quest id " + env.getQuestId());
					}
				} else if (dialogActionId == SELECTED_QUEST_NOREWARD) {
					rewardIndex = env.getExtendedRewardIndex() - 8;
					boolean isLastRepeat = qs.getCompleteCount() == template.getRewardRepeatCount() - 1;
					if (isLastRepeat && template.isSingleTimeClassReward() || template.isClassRewardOnEveryRepeat()) {
						if (rewardIndex >= 0 && rewardIndex < template.getSelectableRewardByClass(playerClass).size()) {
							questItems.add(template.getSelectableRewardByClass(playerClass).get(rewardIndex));
						} else {
							log.warn("The SelectableRewardByClass list has no element on index " + rewardIndex + ". See quest id " + env.getQuestId(),
								new Throwable());
						}
					}
				}
			}
			if (template.getBonus() != null) {
				// Handler can add additional bonuses on repeat (for event quests no data)
				HandlerResult result = QuestEngine.getInstance().onBonusApplyEvent(env, template.getBonus().getType(), questItems);
				if (result != HandlerResult.FAILED) {
					QuestItems additional = BonusService.getQuestBonus(player, template);
					if (additional != null)
						questItems.add(additional);
				}
			}
		}

		return questItems;
	}

	/**
	 * Converts the dialog action ID to the corresponding reward ID.
	 * 
	 * @return The reward index selected, starting at 0. -1 if this action is no reward action.
	 */
	public static int getRewardIndex(int dialogActionId) {
		return dialogActionId >= SELECTED_QUEST_REWARD1 && dialogActionId <= SELECTED_QUEST_REWARD15 ? dialogActionId - SELECTED_QUEST_REWARD1 : -1;
	}

	private static void giveReward(QuestEnv env, Rewards rewards) {
		Player player = env.getPlayer();
		if (rewards.getGold() != null)
			player.getInventory().increaseKinah(Rates.QUEST_KINAH.calcResult(player, rewards.getGold()), ItemUpdateType.INC_KINAH_QUEST);
		if (rewards.getExp() != null) {
			NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(env.getTargetId());
			player.getCommonData().addExp(rewards.getExp(), Rates.XP_QUEST, npcTemplate != null ? npcTemplate.getL10n() : null);
		}
		if (rewards.getTitle() != null)
			player.getTitleList().addTitle(rewards.getTitle(), true, 0);
		if (rewards.getAp() != null) {
			int ap = rewards.getAp();
			if (DataManager.QUEST_DATA.getQuestById(env.getQuestId()).getCategory() != QuestCategory.NON_COUNT) // don't multiply with quest rates for relic
																																																					// exchanges
				ap = (int) Rates.AP_QUEST.calcResult(player, ap);
			AbyssPointsService.addAp(player, ap);
		}
		if (rewards.getDp() != null)
			player.getCommonData().addDp(rewards.getDp());
		if (rewards.getGp() != null)
			GloryPointsService.modifyGpBy(player.getObjectId(), rewards.getGp());
		if (rewards.getExtendInventory() != null) {
			if (rewards.getExtendInventory() == 1)
				CubeExpandService.questExpand(player);
			else if (rewards.getExtendInventory() == 2)
				WarehouseService.expand(player, false);
		}
	}

	private static Timestamp calculateRepeatDate(Player player, QuestTemplate template) {
		ZonedDateTime now = ServerTime.now();
		ZonedDateTime repeatDate = now.with(LocalTime.of(9, 0));
		if (now.isAfter(repeatDate))
			repeatDate = repeatDate.plusDays(1);
		if (template.isDaily()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_QUEST_LIMIT_START_DAILY(9));
		} else {
			DayOfWeek baseDay = repeatDate.getDayOfWeek();
			QuestRepeatCycle nextRepeatDay = findNextRepeatDay(template.getRepeatCycle(), baseDay);
			if (nextRepeatDay.getDay() >= baseDay.getValue())
				repeatDate = repeatDate.plusDays(nextRepeatDay.getDay() - baseDay.getValue());
			else
				repeatDate = repeatDate.plusDays((7 - baseDay.getValue()) + nextRepeatDay.getDay());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_QUEST_LIMIT_START_WEEK(nextRepeatDay.getL10n(), 9));
		}
		return new Timestamp(repeatDate.toEpochSecond() * 1000);
	}

	private static QuestRepeatCycle findNextRepeatDay(List<QuestRepeatCycle> questRepeatDays, DayOfWeek day) {
		Comparator<QuestRepeatCycle> ascendingComparator = Comparator.comparingInt(QuestRepeatCycle::getDay);
		List<QuestRepeatCycle> resetDaysSorted = questRepeatDays.stream().sorted(ascendingComparator).collect(Collectors.toList());
		for (QuestRepeatCycle resetDay : resetDaysSorted) {
			if (resetDay.getDay() >= day.getValue())
				return resetDay;
		}
		return resetDaysSorted.get(0);
	}

	/**
	 * @see #checkStartConditions(Player, int, boolean, int, boolean, boolean, boolean)
	 */
	public static boolean checkStartConditions(Player player, int questId, boolean warn) {
		return checkStartConditions(player, questId, warn, 0, false, false, false);
	}

	/**
	 * Checks if the player meets all required conditions to start the specified quest.<br>
	 * This method will not propagate any exceptions to the caller
	 * 
	 * @param player
	 *          - Player who wants to start the quest
	 * @param questId
	 *          - Concerned quest ID
	 * @param warn
	 *          - Whether to notify the player with a system message on fail
	 * @param skipStartedCheck
	 *          - Whether to ignore if the quest is active
	 * @param skipRepeatCountCheck
	 *          - Whether to ignore if the quest cannot be repeated (anymore)
	 * @param skipXmlPreconditionCheck
	 *          - Whether to ignore preconditions of this quest (from quest_data.xml)
	 * @param allowedDiffToMinLevel
	 *          - Allowed difference between the minimum required level for this quest and the players level
	 * @return True, if the player is allowed to start the quest.
	 */
	public static boolean checkStartConditions(Player player, int questId, boolean warn, int allowedDiffToMinLevel, boolean skipStartedCheck,
		boolean skipRepeatCountCheck, boolean skipXmlPreconditionCheck) {
		try {
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null) {
				if (!skipStartedCheck && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD)) {
					if (warn)
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_WORKING_QUEST());
					return false;
				} else if (!skipRepeatCountCheck && qs.getStatus() == QuestStatus.COMPLETE && !qs.canRepeat()) {
					QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
					if (template.getMaxRepeatCount() > 1 && template.getMaxRepeatCount() != 255 && qs.getCompleteCount() >= template.getMaxRepeatCount()) {
						if (warn)
							PacketSendUtility.sendPacket(player,
								SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_MAX_REPEAT_COUNT(ChatUtil.quest(questId), template.getMaxRepeatCount()));
					} else {
						if (warn)
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_NONE_REPEATABLE(ChatUtil.quest(questId)));
					}
					return false;
				}
			}

			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
			if (template.getRacePermitted() != null && template.getRacePermitted() != Race.PC_ALL && template.getRacePermitted() != player.getRace()) {
				if (warn)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_RACE());
				return false;
			}

			// min level - 2 so that the gray quest arrow shows when quest is almost available
			int levelDiff = template.getMinlevelPermitted() - allowedDiffToMinLevel - player.getLevel();
			if (levelDiff > 0) {
				if (warn)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_MIN_LEVEL(template.getMinlevelPermitted()));
				return false;
			}

			if (template.getMaxlevelPermitted() != 0 && player.getLevel() > template.getMaxlevelPermitted()) {
				if (warn)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_MAX_LEVEL(template.getMaxlevelPermitted()));
				return false;
			}

			if (!template.getClassPermitted().isEmpty() && !template.getClassPermitted().contains(player.getPlayerClass())) {
				if (warn)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_CLASS());
				return false;
			}

			if (template.getGenderPermitted() != null && template.getGenderPermitted() != player.getGender()) {
				if (warn)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_GENDER());
				return false;
			}

			if (template.getRequiredRank() != 0 && player.getAbyssRank().getRank().getId() < template.getRequiredRank()) {
				if (warn)
					PacketSendUtility.sendPacket(player,
						SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_MIN_RANK(AbyssRankEnum.getRankL10n(player.getRace(), template.getRequiredRank())));
				return false;
			}

			if (!skipXmlPreconditionCheck) {
				int fulfilledStartConditions = 0;
				for (XMLStartCondition startCondition : template.getXMLStartConditions()) {
					if (startCondition.check(player, warn))
						fulfilledStartConditions++;
				}
				if (fulfilledStartConditions < template.getRequiredConditionCount())
					return false;
			}

			QuestEnv env = new QuestEnv(null, player, questId);
			if (!inventoryItemCheck(env, warn))
				return false;

			if (!checkCombineSkill(env, warn))
				return false;

			// check if NpcFaction daily quest
			if (template.getNpcFactionId() != 0) {
				// check if the NpcFaction daily time limit has passed
				if (!template.isTimeBased() && !player.getNpcFactions().canStartQuest(template))
					return false;

				NpcFaction faction = player.getNpcFactions().getFactionById(template.getNpcFactionId());
				if (faction == null || !faction.isActive())
					return false;
			}

			return true;
		} catch (Exception ex) {
			log.error("QE: exception in checkStartCondition (" + player + ", questId " + questId + ")", ex);
		}
		return false;
	}

	public static boolean startQuest(QuestEnv env) {
		return startQuest(env, QuestStatus.START, env.getDialogActionId() != NULL);
	}

	public static boolean startQuest(QuestEnv env, QuestStatus status, boolean warn) {
		Player player = env.getPlayer();
		int id = env.getQuestId();
		QuestStateList qsl = player.getQuestStateList();
		QuestState qs = qsl.getQuestState(id);
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
		if (template.getNpcFactionId() != 0) {
			NpcFaction faction = player.getNpcFactions().getFactionById(template.getNpcFactionId());
			if (!faction.isActive() || faction.getQuestId() != id) {
				AuditLogger.log(player, "possibly used packet hack to start npc faction quest");
				return false;
			}
		}
		if (!checkStartConditions(player, id, warn))
			return false;

		if (!template.isNoCount() && !checkQuestListSize(qsl) && !player.hasPermission(MembershipConfig.QUEST_LIMIT_DISABLED)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_MAX_NORMAL());
			return false;
		}

		ActionType actionType;
		if (qs != null) {
			actionType = qs.getStatus() == QuestStatus.COMPLETE ? ActionType.ADD : ActionType.UPDATE;
			qs.setStatus(status);
		} else {
			actionType = ActionType.ADD;
			qs = new QuestState(id, status);
			player.getQuestStateList().addQuest(id, qs);
		}

		if (template.getNpcFactionId() != 0 && !template.isTimeBased()) {
			player.getNpcFactions().startQuest(template);
		}
		if (template.getCategory() == QuestCategory.CHALLENGE_TASK)
			ChallengeTaskService.getInstance().onAcceptTask(player, id);

		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(actionType, qs));
		player.getController().updateNearbyQuests();
		return true;
	}

	/**
	 * Adds the quest to the players quest list.
	 */
	public static void addOrUpdateQuest(Player player, int questId, QuestStatus status) {
		ActionType actionType;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			actionType = ActionType.ADD;
			qs = new QuestState(questId, status);
			player.getQuestStateList().addQuest(questId, qs);
		} else {
			if (qs.getStatus() == status)
				return;
			actionType = qs.getStatus() == QuestStatus.COMPLETE ? ActionType.ADD : ActionType.UPDATE;
			qs.setStatus(status);
			if (status == QuestStatus.COMPLETE)
				qs.setQuestVar(0);
		}
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(actionType, qs));
	}

	/**
	 * Checks if the crafting/tapping skill point requirements for this quest
	 * 
	 * @return True, if the quest skill requirement meets the players skill points.
	 */
	public static boolean checkCombineSkill(QuestEnv env, boolean warn) {
		Player player = env.getPlayer();
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(env.getQuestId());

		if (template == null)
			return false;

		if (template.getCombineSkill() != 0) {
			List<Integer> skills = new ArrayList<>(); // skills to check
			if (template.getCombineSkill() == -1) { // any skill
				if (template.getNpcFactionId() != 12 && template.getNpcFactionId() != 13) { // exclude essence/aether tapping for crafting dailies
					skills.add(30002);
					skills.add(30003);
				}
				skills.add(40001);
				skills.add(40002);
				skills.add(40003);
				skills.add(40004);
				skills.add(40007);
				skills.add(40008);
				skills.add(40010);
			} else {
				skills.add(template.getCombineSkill());
			}
			boolean result = false;
			for (int skillId : skills) {
				PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
				if (skill != null && skill.getSkillLevel() >= template.getCombineSkillPoint()) {
					if (template.getCategory() == QuestCategory.TASK && skill.getSkillLevel() - 40 > template.getCombineSkillPoint())
						continue;
					result = true;
					break;
				}
			}
			if (!result) {
				if (warn)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_TS_RANK(Integer.toString(template.getCombineSkillPoint())));
				return false;
			}
		}

		return true;
	}

	public static boolean startEventQuest(QuestEnv env, QuestStatus questStatus) {
		int id = env.getQuestId();
		Player player = env.getPlayer();
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
		if (template.getCategory() != QuestCategory.EVENT)
			return false;

		if (!checkLevelRequirement(template, player.getLevel()))
			return false;

		if (template.getRacePermitted() == player.getOppositeRace())
			return false;

		if (!template.getClassPermitted().isEmpty())
			if (!template.getClassPermitted().contains(player.getCommonData().getPlayerClass()))
				return false;

		if (template.getGenderPermitted() != null && template.getGenderPermitted() != player.getGender())
			return false;

		QuestState qs = player.getQuestStateList().getQuestState(id);
		if (qs == null) {
			qs = new QuestState(template.getId(), questStatus);
			player.getQuestStateList().addQuest(id, qs);
		} else {
			qs.setStatus(questStatus);
			qs.setQuestVar(0);
			qs.setRewardGroup(null);
		}
		return true;
	}

	/*
	 * Check the player's quest list size for starting a new one
	 * @param quest state list
	 */
	private static boolean checkQuestListSize(QuestStateList qsl) {
		// The player's quest list size + the new one to start
		return (qsl.getNormalQuests().size() + 1) <= CustomConfig.BASIC_QUEST_SIZE_LIMIT;
	}

	public static boolean collectItemCheck(QuestEnv env, boolean removeItem) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(env.getQuestId());
		if (qs == null && removeItem)
			return false;
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(env.getQuestId());
		CollectItems collectItems = template.getCollectItems();
		if (collectItems == null) {
			// check inventoryItems to prevent exploits
			InventoryItems inventoryItems = template.getInventoryItems();
			if (inventoryItems == null)
				return true;

			for (InventoryItem inventoryItem : inventoryItems.getInventoryItems()) {
				int itemId = inventoryItem.getItemId();
				if (player.getInventory().getItemCountByItemId(itemId) < inventoryItem.getCount())
					return false;
			}

			if (removeItem) {
				for (InventoryItem inventoryItem : inventoryItems.getInventoryItems()) {
					player.getInventory().decreaseByItemId(inventoryItem.getItemId(), inventoryItem.getCount());
				}
			}
			return true;
		}

		for (CollectItem collectItem : collectItems.getCollectItem()) {
			int itemId = collectItem.getItemId();
			long count = itemId == ItemId.KINAH ? player.getInventory().getKinah() : player.getInventory().getItemCountByItemId(itemId);
			if (collectItem.getCount() > count)
				return false;
		}
		if (removeItem) {
			for (CollectItem collectItem : collectItems.getCollectItem()) {
				if (collectItem.getItemId() == ItemId.KINAH)
					player.getInventory().decreaseKinah(collectItem.getCount());
				else {
					player.getInventory().decreaseByItemId(collectItem.getItemId(), collectItem.getCount());
				}
			}
		}
		return true;
	}

	public static boolean inventoryItemCheck(QuestEnv env, boolean showWarning) {
		Player player = env.getPlayer();
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(env.getQuestId());
		InventoryItems inventoryItems = template.getInventoryItems();
		if (inventoryItems != null) {
			// Usually counts are 1, and if more, then collect item checks exist
			// Other quests having no collect item checks and counts greater than 1 are unused (old coin exchange quests)
			for (InventoryItem inventoryItem : inventoryItems.getInventoryItems()) {
				if (player.getInventory().getFirstItemByItemId(inventoryItem.getItemId()) == null) {
					if (showWarning) {
						String requiredItemL10n = DataManager.ITEM_DATA.getItemTemplate(inventoryItem.getItemId()).getL10n();
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_INVENTORY_ITEM(requiredItemL10n));
					}
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Only used by relic reward quests. Checks if the player has any necessary items with sufficient count and starts the quest.
	 */
	public static int checkAndGetCollectItemQuestRewardCategory(QuestEnv env) {
		return checkAndGetCollectItemQuestRewardCategory(env, null);
	}

	public static int checkAndGetCollectItemQuestRewardCategory(QuestEnv env, Integer rewardIndex) {
		Player player = env.getPlayer();
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(env.getQuestId());

		CollectItems collectItems = template.getCollectItems();
		if (collectItems == null || template.getRewards().isEmpty() || rewardIndex != null && rewardIndex >= template.getRewards().size())
			return -1;

		if (rewardIndex == null) { // Verify if player has atleast one item with sufficient count and starts quest
			for (CollectItem cItem : collectItems.getCollectItem()) {
				if (player.getInventory().getItemCountByItemId(cItem.getItemId()) >= cItem.getCount()) {
					QuestState qs = player.getQuestStateList().getQuestState(env.getQuestId());
					if (qs == null || qs.isStartable()) {
						boolean stateValid = true;
						if (collectItems.getStartCheck())
							stateValid = startQuest(env);
						if (stateValid)
							return 0;
					} else if (qs.getStatus() != QuestStatus.START && collectItems.getStartCheck()) {
						return -1;
					}
				}
			}
		} else {
			CollectItem selectedOption = collectItems.getCollectItem().get(rewardIndex);
			if (player.getInventory().getItemCountByItemId(selectedOption.getItemId()) < selectedOption.getCount()
				|| !player.getInventory().decreaseByItemId(selectedOption.getItemId(), selectedOption.getCount())) {
				String requiredItemL10n = DataManager.ITEM_DATA.getItemTemplate(selectedOption.getItemId()).getL10n();
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_QUEST_COMPLETE_ERROR_QUEST_ITEM_RETRY(requiredItemL10n));
				return -1;
			} else {
				return rewardIndex;
			}
		}
		return -1;
	}

	public static int getQuestDrop(Set<DropItem> dropItems, int index, Npc npc, Collection<Player> players, Player player) {
		Collection<QuestDrop> drops = getQuestDrop(npc.getNpcId());
		if (drops.isEmpty()) {
			return index;
		}
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npc.getObjectId());
		for (QuestDrop drop : drops) {
			if (Rnd.chance() >= drop.getChance())
				continue;

			if (players != null && player.isInGroup()) {
				List<Player> pls = new ArrayList<>();
				if (drop.isDropEachMemberGroup()) {
					for (Player member : players) {
						if (isQuestDrop(member, drop)) {
							pls.add(member);
							dropItems.add(regQuestDropItem(drop, index++, member.getObjectId()));
						}
					}
				} else {
					for (Player member : players) {
						if (isQuestDrop(member, drop)) {
							pls.add(member);
							break;
						}
					}
				}
				if (pls.size() > 0) {
					DropItem dItem = null;
					if (!drop.isDropEachMemberGroup()) {
						dItem = regQuestDropItem(drop, index++, 0);
						dropItems.add(dItem);
					}
					for (Player p : pls) {
						if (dItem != null) {
							dItem.setPlayerObjId(p.getObjectId());
						}
						dropNpc.setAllowedLooter(p);
						if (player.getPlayerGroup().getLootGroupRules().getLootRule() != LootRuleType.FREEFORALL) {
							PacketSendUtility.sendPacket(p, new SM_LOOT_STATUS(npc.getObjectId(), Status.LOOT_ENABLE));
						}
					}
					pls.clear();
				}
			} else if (players != null && player.isInAlliance()) {
				List<Player> pls = new ArrayList<>();
				if (drop.isDropEachMemberAlliance()) {
					for (Player member : players) {
						if (isQuestDrop(member, drop)) {
							pls.add(member);
							dropItems.add(regQuestDropItem(drop, index++, member.getObjectId()));
						}
					}
				} else {
					for (Player member : players) {
						if (isQuestDrop(member, drop)) {
							pls.add(member);
							break;
						}
					}
				}
				if (pls.size() > 0) {
					DropItem dItem = null;
					if (!drop.isDropEachMemberAlliance()) {
						dItem = regQuestDropItem(drop, index++, 0);
						dropItems.add(dItem);
					}
					for (Player p : pls) {
						if (dItem != null) {
							dItem.setPlayerObjId(p.getObjectId());
						}
						dropNpc.setAllowedLooter(p);
						if (player.getPlayerAlliance().getLootGroupRules().getLootRule() != LootRuleType.FREEFORALL) {
							PacketSendUtility.sendPacket(p, new SM_LOOT_STATUS(npc.getObjectId(), Status.LOOT_ENABLE));
						}
					}
					pls.clear();
				}
			} else {
				if (isQuestDrop(player, drop)) {
					dropItems.add(regQuestDropItem(drop, index++, player.getObjectId()));
				}
			}
		}
		return index;
	}

	private static DropItem regQuestDropItem(QuestDrop drop, int index, Integer winner) {
		DropItem item = new DropItem(new Drop(drop.getItemId(), 1, 1, drop.getChance()));
		item.setPlayerObjId(winner);
		item.setIndex(index);
		item.setCount(1);
		return item;
	}

	private static boolean isQuestDrop(Player player, QuestDrop drop) {
		int questId = drop.getQuestId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (drop.getCollectingStep() != 0) {
			if (drop.getCollectingStep() != qs.getQuestVarById(0)) {
				return false;
			}
		}
		QuestTemplate qt = DataManager.QUEST_DATA.getQuestById(questId);
		if (qt.getTarget().equals(QuestTarget.ALLIANCE)) {
			if (!player.isInAlliance()) {
				return false;
			}
		}
		if (qt.getMentorType() == QuestMentorType.MENTE) {
			if (!player.isInGroup()) {
				return false;
			}

			PlayerGroup group = player.getPlayerGroup();
			if (group.getMembers().stream().noneMatch(member -> member.isMentor() && PositionUtil.isInRange(player, member, GroupConfig.GROUP_MAX_DISTANCE))) {
				return false;
			}
		}
		if (drop instanceof HandlerSideDrop handlerSideDrop) {
			return handlerSideDrop.getNeededAmount() > player.getInventory().getItemCountByItemId(drop.getItemId());
		}

		CollectItems collectItems = DataManager.QUEST_DATA.getQuestById(questId).getCollectItems();
		if (collectItems == null)
			return true;

		for (CollectItem collectItem : collectItems.getCollectItem()) {
			int collectItemId = collectItem.getItemId();
			long count = player.getInventory().getItemCountByItemId(collectItemId);
			if (collectItem.getCount() > count && drop.getItemId() == collectItemId)
				return true;
		}
		return false;
	}

	public static boolean checkLevelRequirement(int questId, int playerLevel) {
		return checkLevelRequirement(DataManager.QUEST_DATA.getQuestById(questId), playerLevel);
	}

	public static boolean checkLevelRequirement(QuestTemplate qt, int playerLevel) {
		return playerLevel >= qt.getMinlevelPermitted() && (qt.getMaxlevelPermitted() == 0 || playerLevel <= qt.getMaxlevelPermitted());
	}

	public static int getLevelRequirementDiff(int questId, int playerLevel) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		return template == null ? 99 : template.getMinlevelPermitted() - playerLevel;
	}

	public static boolean questTimerStart(QuestEnv env, int timeInSeconds) {
		final Player player = env.getPlayer();

		// Schedule Action When Timer Finishes
		Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				QuestEngine.getInstance().onQuestTimerEnd(new QuestEnv(null, player, 0));
			}
		}, timeInSeconds * 1000);
		player.getController().addTask(TaskId.QUEST_TIMER, task);
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(env.getQuestId(), timeInSeconds));
		return true;
	}

	public static boolean invisibleTimerStart(QuestEnv env, int timeInSeconds) {
		final Player player = env.getPlayer();

		// Schedule Action When Timer Finishes
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				QuestEngine.getInstance().onInvisibleTimerEnd(new QuestEnv(null, player, 0));
			}
		}, timeInSeconds * 1000);
		return true;
	}

	public static boolean questTimerEnd(QuestEnv env) {
		final Player player = env.getPlayer();

		player.getController().cancelTask(TaskId.QUEST_TIMER);
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(env.getQuestId(), 0));
		return true;
	}

	public static boolean abandonQuest(Player player, int questId) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		if (template == null)
			return false;

		if (template.isCannotGiveup())
			return false;

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.COMPLETE || qs.getStatus() == QuestStatus.LOCKED)
			return false;

		if (qs.getCompleteCount() > 0) { // set back to complete if it was completed at least once
			qs.setStatus(QuestStatus.COMPLETE, false);
			qs.setQuestVar(0);
			qs.setFlags(0);
		} else { // entirely delete from players quest list
			player.getQuestStateList().deleteQuest(questId);
		}

		if (template.getNpcFactionId() != 0)
			player.getNpcFactions().abortQuest(template);

		removeQuestWorkItems(player, qs);
		if (template.getCategory() == QuestCategory.TASK) {
			XMLQuest xmlQuest = DataManager.XML_QUESTS.getQuest(questId);
			if (xmlQuest instanceof WorkOrdersData)
				player.getRecipeList().deleteRecipe(player, ((WorkOrdersData) xmlQuest).getRecipeId());
		}

		if (player.getController().hasTask(TaskId.QUEST_TIMER))
			questTimerEnd(new QuestEnv(null, player, questId));

		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.ABANDON, qs));
		player.getController().updateNearbyQuests();
		return true;
	}

	public static Collection<QuestDrop> getQuestDrop(int npcId) {
		return questDrop.getOrDefault(npcId, Collections.emptyList());
	}

	public static void addQuestDrop(int npcId, QuestDrop drop) {
		List<QuestDrop> drops = questDrop.computeIfAbsent(npcId, k -> new ArrayList<>());
		drops.add(drop);
	}

	/**
	 * Clears all quest drop info (used when reloading quest data)
	 */
	public static void clearQuestDrops() {
		questDrop.clear();
	}

	public static List<Player> getEachDropMembersGroup(PlayerGroup group, int npcId, int questId) {
		List<Player> players = new ArrayList<>();
		for (QuestDrop qd : getQuestDrop(npcId)) {
			if (qd.isDropEachMemberGroup()) {
				for (Player player : group.getMembers()) {
					QuestState qstel = player.getQuestStateList().getQuestState(questId);
					if (qstel != null && qstel.getStatus() == QuestStatus.START) {
						players.add(player);
					}
				}
				break;
			}
		}
		return players;
	}

	public static List<Player> getEachDropMembersAlliance(PlayerAlliance alliance, int npcId, int questId) {
		List<Player> players = new ArrayList<>();
		for (QuestDrop qd : getQuestDrop(npcId)) {
			if (qd.isDropEachMemberGroup()) {
				for (Player player : alliance.getMembers()) {
					QuestState qstel = player.getQuestStateList().getQuestState(questId);
					if (qstel != null && qstel.getStatus() == QuestStatus.START) {
						players.add(player);
					}
				}
				break;
			}
		}
		return players;
	}

	public static void removeQuestWorkItems(Player player, QuestState qs) {
		QuestWorkItems qwi = DataManager.QUEST_DATA.getQuestById(qs.getQuestId()).getQuestWorkItems();
		if (qwi != null) {
			for (QuestItems qi : qwi.getQuestWorkItem()) {
				if (qi != null) {
					long count = player.getInventory().getItemCountByItemId(qi.getItemId());
					if (count > 0)
						player.getInventory().decreaseByItemId(qi.getItemId(), count, qs.getStatus());
				}
			}
		}
	}
}
