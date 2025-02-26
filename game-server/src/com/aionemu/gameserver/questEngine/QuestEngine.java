package com.aionemu.gameserver.questEngine;

import java.util.*;
import java.util.stream.Collectors;

import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.ScriptManager;
import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.GameEngine;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.factions.NpcFactionTemplate;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.quest.*;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_COMPLETED_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandlerLoader;
import com.aionemu.gameserver.questEngine.handlers.models.XMLQuest;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.collections.DynamicServerPacketBodySplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke, Hilgert, vlog, Neon
 */
public class QuestEngine implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(QuestEngine.class);
	private final ScriptManager scriptManager = new ScriptManager();
	private JobDetail messageTask;
	private final Map<Integer, AbstractQuestHandler> questHandlers = new HashMap<>();
	private final Map<Integer, QuestNpc> questNpcs = new HashMap<>();
	private final Map<Integer, List<Integer>> questItemRelated = new HashMap<>();
	private final List<Integer> questHouseItems = new ArrayList<>();
	private final Map<Integer, List<Integer>> questItems = new HashMap<>();
	private final List<Integer> questOnCompleted = new ArrayList<>();
	private final Map<Race, List<Integer>> questOnLevelUp = new EnumMap<>(Race.class);
	private final List<Integer> questOnDie = new ArrayList<>();
	private final List<Integer> questOnLogOut = new ArrayList<>();
	private final List<Integer> questOnEnterWorld = new ArrayList<>();
	private final Map<ZoneName, List<Integer>> questOnEnterZone = new HashMap<>();
	private final Map<ZoneName, List<Integer>> questOnLeaveZone = new HashMap<>();
	private final Map<String, List<Integer>> questOnPassFlyingRings = new HashMap<>();
	private final List<Integer> questOnTimerEnd = new ArrayList<>();
	private final List<Integer> onInvisibleTimerEnd = new ArrayList<>();
	private final Map<AbyssRankEnum, List<Integer>> questOnKillRanked = new EnumMap<>(AbyssRankEnum.class);
	private final Map<Integer, List<Integer>> questOnKillInWorld = new HashMap<>();
	private final Map<Integer, List<Integer>> questOnUseSkill = new HashMap<>();
	private final Map<Integer, Integer> questOnFailCraft = new HashMap<>();
	private final Map<Integer, List<Integer>> questOnEquipItem = new HashMap<>();
	private final Map<Integer, List<Integer>> questCanAct = new HashMap<>();
	private final List<Integer> questOnDredgionReward = new ArrayList<>();
	private final Map<BonusType, List<Integer>> questOnBonusApply = new EnumMap<>(BonusType.class);
	private final List<Integer> questUpdateItems = new ArrayList<>();
	private final List<Integer> reachTarget = new ArrayList<>();
	private final List<Integer> lostTarget = new ArrayList<>();
	private final List<Integer> questOnEnterWindStream = new ArrayList<>();
	private final List<Integer> questRideAction = new ArrayList<>();
	private final Map<String, List<Integer>> questOnKillInZone = new HashMap<>();

	private QuestEngine() {
	}

	@Override
	public void init() {
		for (QuestTemplate data : DataManager.QUEST_DATA.getQuestTemplates()) {
			for (QuestDrop drop : data.getQuestDrop()) {
				drop.setQuestId(data.getId());
				QuestService.addQuestDrop(drop.getNpcId(), drop);
			}
			if (data.getInventoryItems() != null) {
				for (InventoryItem inventoryItem : data.getInventoryItems().getInventoryItems()) {
					if (!questUpdateItems.contains(inventoryItem.getItemId()))
						questUpdateItems.add(inventoryItem.getItemId());
				}
			}
		}
		AggregatedClassListener acl = new AggregatedClassListener();
		acl.addClassListener(new OnClassLoadUnloadListener());
		acl.addClassListener(new ScheduledTaskClassListener());
		acl.addClassListener(new QuestHandlerLoader());
		scriptManager.setGlobalClassListener(acl);
		scriptManager.load(GSConfig.QUEST_HANDLER_DIRECTORY);
		for (XMLQuest xmlQuest : DataManager.XML_QUESTS.getAllQuests())
			xmlQuest.register(this);
		log.info("Loaded " + questHandlers.size() + " quest handlers.");
		if (GSConfig.ANALYZE_QUESTHANDLERS)
			analyzeQuestHandlers();
		addMessageSendingTask();
	}

	public void reload() {
		scriptManager.shutdown();
		clear();
		init();
	}

	public void clear() {
		CronService.getInstance().cancel(messageTask);
		QuestService.clearQuestDrops();
		questNpcs.clear();
		questItemRelated.clear();
		questItems.clear();
		questHouseItems.clear();
		questOnLevelUp.clear();
		questOnCompleted.clear();
		questOnEnterWorld.clear();
		questOnDie.clear();
		questOnLogOut.clear();
		questOnEnterZone.clear();
		questOnLeaveZone.clear();
		questOnTimerEnd.clear();
		onInvisibleTimerEnd.clear();
		questOnPassFlyingRings.clear();
		questOnKillRanked.clear();
		questOnKillInWorld.clear();
		questOnKillInZone.clear();
		questOnUseSkill.clear();
		questOnFailCraft.clear();
		questOnEquipItem.clear();
		questCanAct.clear();
		questOnDredgionReward.clear();
		questOnBonusApply.clear();
		questUpdateItems.clear();
		reachTarget.clear();
		lostTarget.clear();
		questOnEnterWindStream.clear();
		questRideAction.clear();
		questHandlers.clear();
	}

	public boolean onDialog(QuestEnv env) {
		try {
			AbstractQuestHandler questHandler;
			if (env.getQuestId() != 0) {
				questHandler = getQuestHandlerByQuestId(env.getQuestId());
				if (questHandler != null)
					if (questHandler.onDialogEvent(env))
						return true;
					else {
						QuestTemplate qt = DataManager.QUEST_DATA.getQuestById(env.getQuestId());
						if (qt != null && qt.getCategory() == QuestCategory.CHALLENGE_TASK)
							PacketSendUtility.sendPacket(env.getPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_QUEST_LIMIT_START_DAILY(9));
					}
			} else {
				Npc npc = (Npc) env.getVisibleObject();
				for (int questId : getQuestNpc(npc == null ? 0 : npc.getNpcId()).getOnTalkEvent()) {
					questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						if (questHandler.onDialogEvent(env))
							return true;
					}
				}
				env.setQuestId(0);
			}
		} catch (Exception ex) {
			log.error("QE: exception in onDialog", ex);
			return false;
		}
		return false;
	}

	public boolean onKill(QuestEnv env) {
		try {
			Npc npc = (Npc) env.getVisibleObject();
			for (int questId : getQuestNpc(npc.getNpcId()).getOnKillEvent()) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onKillEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onKill", ex);
			return false;
		}
		return true;
	}

	public boolean onAttack(QuestEnv env) {
		try {
			Npc npc = (Npc) env.getVisibleObject();
			for (int questId : getQuestNpc(npc.getNpcId()).getOnAttackEvent()) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onAttackEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onAttack", ex);
			return false;
		}
		return true;
	}

	public void sendCompletedQuests(Player player) {
		SplitList<QuestState> questStateSplitList = new DynamicServerPacketBodySplitList<>(player.getQuestStateList().getCompletedQuests(), true,
			SM_QUEST_COMPLETED_LIST.STATIC_BODY_SIZE, SM_QUEST_COMPLETED_LIST.DYNAMIC_BODY_PART_SIZE_CALCULATOR);
		questStateSplitList.forEach(part -> PacketSendUtility.sendPacket(player, new SM_QUEST_COMPLETED_LIST(part.isFirst() ? 0 : 1, part)));
	}

	/**
	 * Notifies all quest handlers (which registered the event), that the player level changed
	 * 
	 * @param player
	 *          - The player who leveled up
	 */
	public void onLevelChanged(Player player) {
		try {
			List<Integer> raceQuestsOnLevelUp = getOrCreateOnLevelUpForRace(player.getRace());
			for (int questId : raceQuestsOnLevelUp) {
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs == null || qs.getStatus() != QuestStatus.COMPLETE) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null)
						questHandler.onLevelChangedEvent(player);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onLevelChanged", ex);
		}
	}

	/**
	 * Notifies all quest handlers (which registered the event), that the quest with the specified ID completed
	 * 
	 * @param player
	 *          - Player who completed the quest
	 * @param questId
	 *          - The quest that the player completed
	 */
	public void onQuestCompleted(Player player, int questId) {
		try {
			QuestEnv env = new QuestEnv(null, player, questId);
			for (Integer onCompletedQuestId : questOnCompleted) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(onCompletedQuestId);
				if (questHandler != null)
					questHandler.onQuestCompletedEvent(env);
			}
		} catch (Exception ex) {
			log.error("QE: exception in onQuestCompleted", ex);
		}
	}

	public void onDie(QuestEnv env) {
		try {
			for (int questId : questOnDie) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onDieEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onDie", ex);
		}
	}

	public void onLogOut(QuestEnv env) {
		try {
			for (int questId : questOnLogOut) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onLogOutEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onLogOut", ex);
		}
	}

	public void onNpcReachTarget(QuestEnv env) {
		try {
			for (int questId : reachTarget) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onNpcReachTargetEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onNpcReachTarget", ex);
		}
	}

	public void onNpcLostTarget(QuestEnv env) {
		try {
			for (int questId : lostTarget) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onNpcLostTargetEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onNpcLostTarget", ex);
		}
	}

	public void onPassFlyingRing(QuestEnv env, String flyRing) {
		try {
			List<Integer> questIds = questOnPassFlyingRings.get(flyRing);
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						questHandler.onPassFlyingRingEvent(env, flyRing);
					}
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onFlyRingPassEvent", ex);
		}
	}

	public void onEnterWorld(Player player) {
		try {
			for (int questId : questOnEnterWorld) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null)
					questHandler.onEnterWorldEvent(new QuestEnv(null, player, questId));
			}
		} catch (Exception ex) {
			log.error("QE: exception in onEnterWorld", ex);
		}
	}

	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		try {
			List<Integer> questIds = questItemRelated.get(item.getItemId());
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						HandlerResult result = questHandler.onItemUseEvent(env, item);
						// allow other quests to process, the same item can be used in multiple quests
						if (result != HandlerResult.UNKNOWN)
							return result;
					}
				}
			}
			return HandlerResult.UNKNOWN;
		} catch (Exception ex) {
			log.error("QE: exception in onItemUseEvent", ex);
			return HandlerResult.FAILED;
		}
	}

	public void onHouseItemUseEvent(QuestEnv env) {
		try {
			for (Integer questHouseItem : questHouseItems) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questHouseItem);
				if (questHandler != null) {
					env.setQuestId(questHouseItem);
					questHandler.onHouseItemUseEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onHouseItemUseEvent", ex);
		}
	}

	public void onItemGet(Player player, int itemId) {
		List<Integer> questIds = questItems.get(itemId);
		if (questIds != null) {
			for (int i = 0; i < questIds.size(); i++) {
				int questId = questItems.get(itemId).get(i);
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null)
					questHandler.onGetItemEvent(new QuestEnv(null, player, questId));
			}
		}
		if (questUpdateItems.contains(itemId))
			player.getController().updateNearbyQuests();
	}

	public void onItemRemoved(Player player, int itemId) {
		if (questUpdateItems.contains(itemId))
			player.getController().updateNearbyQuests();
	}

	public boolean onKillRanked(QuestEnv env, AbyssRankEnum playerRank) {
		try {
			if (playerRank != null) {
				List<Integer> questIds = questOnKillRanked.get(playerRank);
				if (questIds != null) {
					for (int questId : questIds) {
						AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
						if (questHandler != null) {
							env.setQuestId(questId);
							questHandler.onKillRankedEvent(env);
						}
					}
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onKillRanked", ex);
			return false;
		}
		return true;
	}

	public boolean onKillInWorld(QuestEnv env, int worldId) {
		try {
			List<Integer> questIds = questOnKillInWorld.get(worldId);
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						questHandler.onKillInWorldEvent(env);
					}
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onKillInWorld", ex);
			return false;
		}
		return true;
	}

	public boolean onKillInZone(QuestEnv env, String zoneName) {
		try {
			List<Integer> questIds = questOnKillInZone.get(zoneName);
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						questHandler.onKillInZoneEvent(env);
					}
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onKillInZone", ex);
			return false;
		}
		return true;
	}

	public boolean onEnterZone(QuestEnv env, ZoneName zoneName) {
		try {
			List<Integer> questIds = questOnEnterZone.get(zoneName);
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						questHandler.onEnterZoneEvent(env, zoneName);
					}
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onEnterZone", ex);
			return false;
		}
		return true;
	}

	public boolean onLeaveZone(QuestEnv env, ZoneName zoneName) {
		try {
			List<Integer> questIds = questOnLeaveZone.get(zoneName);
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						questHandler.onLeaveZoneEvent(env, zoneName);
					}
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onLeaveZone", ex);
			return false;
		}
		return true;
	}

	public void onMovieEnd(QuestEnv env, int movieId) {
		try {
			AbstractQuestHandler questHandler = getQuestHandlerByQuestId(env.getQuestId());
			if (questHandler != null)
				questHandler.onMovieEndEvent(env, movieId);
		} catch (Exception ex) {
			log.error("QE: exception in onMovieEnd", ex);
		}
	}

	public void onQuestTimerEnd(QuestEnv env) {
		try {
			for (int questId : questOnTimerEnd) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onQuestTimerEndEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onQuestTimerEnd", ex);
		}
	}

	public void onInvisibleTimerEnd(QuestEnv env) {
		try {
			for (int questId : onInvisibleTimerEnd) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onInvisibleTimerEndEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onInvisibleTimerEnd", ex);
		}
	}

	public boolean onUseSkill(QuestEnv env, int skillId) {
		try {
			List<Integer> questIds = questOnUseSkill.get(skillId);
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						questHandler.onUseSkillEvent(env, skillId);
					}
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onUseSkill", ex);
			return false;
		}
		return true;
	}

	public void onFailCraft(QuestEnv env, int itemId) {
		Integer questId = questOnFailCraft.get(itemId);
		if (questId != null) {
			AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if (questHandler != null) {
				if (env.getPlayer().getInventory().getItemCountByItemId(itemId) == 0) {
					env.setQuestId(questId);
					questHandler.onFailCraftEvent(env, itemId);
				}
			}
		}
	}

	public void onEquipItem(QuestEnv env, int itemId) {
		List<Integer> questIds = questOnEquipItem.get(itemId);
		if (questIds != null) {
			for (int questId : questIds) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onEquipItemEvent(env, itemId);
				}
			}
		}
	}

	public boolean onCanAct(QuestEnv env, int templateId, QuestActionType questActionType, Object... objects) {
		List<Integer> questIds = questCanAct.get(templateId);
		if (questIds != null) {
			for (int questId : questIds) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					if (questHandler.onCanAct(env, questActionType, objects))
						return true;
				}
			}
		}
		return false;
	}

	public void onDredgionReward(QuestEnv env) {
		try {
			for (int questId : questOnDredgionReward) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onDredgionRewardEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onDredgionReward", ex);
		}
	}

	public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
		try {
			List<Integer> questIds = questOnBonusApply.get(bonusType);
			if (questIds != null) {
				for (int questId : questIds) {
					AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
					if (questHandler != null) {
						env.setQuestId(questId);
						return questHandler.onBonusApplyEvent(env, bonusType, rewardItems);
					}
				}
			}
			return HandlerResult.UNKNOWN;
		} catch (Exception ex) {
			log.error("QE: exception in onBonusApply", ex);
			return HandlerResult.FAILED;
		}
	}

	public boolean onAddAggroList(QuestEnv env) {
		try {
			Npc npc = (Npc) env.getVisibleObject();
			for (int questId : getQuestNpc(npc.getNpcId()).getOnAddAggroListEvent()) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onAddAggroListEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onAddAggroList", ex);
			return false;
		}
		return true;
	}

	public boolean onAtDistance(QuestEnv env) {
		QuestNpc questNpc;
		Npc npc = (Npc) env.getVisibleObject();
		if (!questNpcs.containsKey(npc.getNpcId())) {
			return false;
		}
		questNpc = getQuestNpc(npc.getNpcId());
		if (getQuestNpc(npc.getNpcId()).getOnDistanceEvent().size() == 0)
			return false;
		Player player = env.getPlayer();
		if (!PositionUtil.isInRange(npc, player, questNpc.getQuestRange()))
			return false;
		try {
			for (int questId : questNpc.getOnDistanceEvent()) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onAtDistanceEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onAtDistance", ex);
			return false;
		}
		return true;
	}

	public void onEnterWindStream(QuestEnv env, int loc) {
		try {
			for (int questId : questOnEnterWindStream) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onEnterWindStreamEvent(env, loc);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onWindStream", ex);
		}
	}

	public void rideAction(QuestEnv env, int itemId) {
		try {
			for (int questId : questRideAction) {
				AbstractQuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.rideAction(env, itemId);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in rideAction", ex);
		}
	}

	public QuestNpc registerQuestNpc(int npcId) {
		if (!questNpcs.containsKey(npcId)) {
			questNpcs.put(npcId, new QuestNpc(npcId));
		}
		return questNpcs.get(npcId);
	}

	public QuestNpc registerQuestNpc(int npcId, int range) {
		if (!questNpcs.containsKey(npcId)) {
			questNpcs.put(npcId, new QuestNpc(npcId, range));
		}
		return questNpcs.get(npcId);
	}

	public void registerQuestItem(int itemId, int questId) {
		questItemRelated.computeIfAbsent(itemId, k -> new ArrayList<>()).add(questId);
	}

	public boolean isRegisteredQuestItem(int itemId) {
		return questItemRelated.containsKey(itemId);
	}

	public void registerQuestHouseItem(int questId) {
		if (!questHouseItems.contains(questId))
			questHouseItems.add(questId);
	}

	public void registerOnGetItem(int itemId, int questId) {
		questItems.computeIfAbsent(itemId, k -> new ArrayList<>()).add(questId);
	}

	public void registerOnLevelChanged(int questId) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		Race racePermitted = template.getRacePermitted();
		List<Integer> quests;
		if (racePermitted == null) {
			quests = getOrCreateOnLevelUpForRace(Race.ASMODIANS);
			if (!quests.contains(questId))
				quests.add(questId);
			quests = getOrCreateOnLevelUpForRace(Race.ELYOS);
			if (!quests.contains(questId))
				quests.add(questId);
		} else {
			quests = getOrCreateOnLevelUpForRace(racePermitted);
			if (!quests.contains(questId))
				quests.add(questId);
		}
	}

	private List<Integer> getOrCreateOnLevelUpForRace(Race race) {
		return questOnLevelUp.computeIfAbsent(race, k -> new ArrayList<>());
	}

	public void registerOnQuestCompleted(int questId) {
		if (!questOnCompleted.contains(questId))
			questOnCompleted.add(questId);
	}

	public void registerOnEnterWorld(int questId) {
		if (!questOnEnterWorld.contains(questId))
			questOnEnterWorld.add(questId);
	}

	public void registerOnDie(int questId) {
		if (!questOnDie.contains(questId))
			questOnDie.add(questId);
	}

	public void registerOnLogOut(int questId) {
		if (!questOnLogOut.contains(questId))
			questOnLogOut.add(questId);
	}

	public void registerOnEnterZone(ZoneName zoneName, int questId) {
		questOnEnterZone.computeIfAbsent(zoneName, k -> new ArrayList<>()).add(questId);
	}

	public void registerOnKillInZone(String zone, int questId) {
		questOnKillInZone.computeIfAbsent(zone, k -> new ArrayList<>()).add(questId);
	}

	public void registerOnLeaveZone(ZoneName zoneName, int questId) {
		questOnLeaveZone.computeIfAbsent(zoneName, k -> new ArrayList<>()).add(questId);
	}

	public void registerOnKillRanked(AbyssRankEnum playerRank, int questId) {
		for (AbyssRankEnum rank : AbyssRankEnum.values()) {
			if (rank.getId() >= playerRank.getId()) {
				questOnKillRanked.computeIfAbsent(rank, k -> new ArrayList<>()).add(questId);
			}
		}
	}

	public void registerOnKillInWorld(int worldId, int questId) {
		questOnKillInWorld.computeIfAbsent(worldId, k -> new ArrayList<>()).add(questId);
	}

	public void registerOnPassFlyingRings(String flyingRing, int questId) {
		questOnPassFlyingRings.computeIfAbsent(flyingRing, k -> new ArrayList<>()).add(questId);
	}

	public void registerOnQuestTimerEnd(int questId) {
		if (!questOnTimerEnd.contains(questId))
			questOnTimerEnd.add(questId);
	}

	public void registerOnInvisibleTimerEnd(int questId) {
		if (!onInvisibleTimerEnd.contains(questId))
			onInvisibleTimerEnd.add(questId);
	}

	public void registerQuestSkill(int skillId, int questId) {
		questOnUseSkill.computeIfAbsent(skillId, k -> new ArrayList<>()).add(questId);
	}

	public void registerOnFailCraft(int itemId, int questId) {
		questOnFailCraft.putIfAbsent(itemId, questId);
	}

	public void registerOnEquipItem(int itemId, int questId) {
		questOnEquipItem.computeIfAbsent(itemId, k -> new ArrayList<>()).add(questId);
	}

	public boolean registerCanAct(int questId, int npcId) {
		NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(npcId);
		if (template == null) {
			log.warn("[QuestEngine] No such NPC template for " + npcId + " in Q" + questId);
			return false;
		}
		if ("quest_use_item".equals(template.getAiName())) {
			questCanAct.computeIfAbsent(npcId, k -> new ArrayList<>()).add(questId);
			return true;
		}
		return false;
	}

	public void registerOnDredgionReward(int questId) {
		if (!questOnDredgionReward.contains(questId))
			questOnDredgionReward.add(questId);
	}

	public void registerOnBonusApply(int questId, BonusType bonusType) {
		questOnBonusApply.computeIfAbsent(bonusType, k -> new ArrayList<>()).add(questId);
	}

	public void registerAddOnReachTargetEvent(int questId) {
		if (!reachTarget.contains(questId))
			reachTarget.add(questId);
	}

	public void registerAddOnLostTargetEvent(int questId) {
		if (!lostTarget.contains(questId))
			lostTarget.add(questId);
	}

	public void registerOnEnterWindStream(int questId) {
		if (!questOnEnterWindStream.contains(questId))
			questOnEnterWindStream.add(questId);
	}

	public void registerOnRide(int questId) {
		if (!questRideAction.contains(questId))
			questRideAction.add(questId);
	}

	public QuestNpc getQuestNpc(int npcId) {
		QuestNpc questNpc = questNpcs.get(npcId);
		if (questNpc != null) {
			return questNpc;
		}
		return new QuestNpc(npcId);
	}

	private AbstractQuestHandler getQuestHandlerByQuestId(int questId) {
		return questHandlers.get(questId);
	}

	public int getQuestHandlerCount() {
		return questHandlers.size();
	}

	public boolean isHaveHandler(int questId) {
		return questHandlers.containsKey(questId);
	}

	public void addQuestHandler(AbstractQuestHandler questHandler) {
		int questId = questHandler.getQuestId();
		if (questHandlers.putIfAbsent(questId, questHandler) != null)
			log.warn("Duplicate handler for quest: " + questId);
		else
			questHandler.register();
	}

	/** Add handler side drop (if not already in xml) */
	public void addHandlerSideQuestDrop(int questId, int npcId, int itemId, int amount, int chance) {
		HandlerSideDrop hsd = new HandlerSideDrop(questId, npcId, itemId, amount, chance);
		QuestService.addQuestDrop(hsd.getNpcId(), hsd);
	}

	public void addHandlerSideQuestDrop(int questId, int npcId, int itemId, int amount, int chance, int step) {
		HandlerSideDrop hsd = new HandlerSideDrop(questId, npcId, itemId, amount, chance, step);
		QuestService.addQuestDrop(hsd.getNpcId(), hsd);
	}

	private void analyzeQuestHandlers() {
		boolean ignoreEventQuests = true;
		log.info("Analyzing quest handlers (ignoreEventQuests=" + ignoreEventQuests + ")...");
		Set<Integer> unobtainableQuests = new HashSet<>();
		Set<Integer> factionIds = new HashSet<>();
		for (NpcFactionTemplate nft : DataManager.NPC_FACTIONS_DATA.getNpcFactionsData()) {
			if (nft.getNpcIds() == null || nft.getNpcIds().stream().anyMatch(this::existsSpawnData))
				factionIds.add(nft.getId());
		}
		for (AbstractQuestHandler qh : questHandlers.values()) {
			QuestTemplate qt = DataManager.QUEST_DATA.getQuestById(qh.getQuestId());
			if (qt.getMinlevelPermitted() == 99 || qt.getNpcFactionId() > 0 && !factionIds.contains(qt.getNpcFactionId()))
				unobtainableQuests.add(qh.getQuestId()); // players can still have these quests from before an update
		}
		Map<String, String> missingSpawnsByQuests = new LinkedHashMap<>();
		questNpcs.forEach((npcId, npc) -> {
			if (!existsSpawnData(npcId)) { // if the npc doesn't appear in any spawn template (world, instance, base, siege, temporary, event, ...)
				Set<Integer> questIds = npc.findAllRegisteredQuestIds();
				if (ignoreEventQuests && questIds.stream().allMatch(id -> id >= 80000))
					return;
				if (questIds.stream().allMatch(id -> unobtainableQuests.contains(id) || existsSpawnDataForAnyAlternativeNpc(id, npcId)))
					return; // don't log unobtainable quests or if alternative npcs appear in spawn data (many quests support outdated + current npcs)
				missingSpawnsByQuests.compute(questIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", ")),
					(k, npcIds) -> npcIds == null ? String.valueOf(npcId) : npcIds + '/' + npcId);
			}
		});
		if (missingSpawnsByQuests.isEmpty())
			log.info("Quest handler analysis finished without errors!");
		else
			log.warn("Missing quest npc spawns:{}", missingSpawnsByQuests.entrySet().stream()
				.map(e -> "\n\tNpc " + e.getValue() + " (quests: " + e.getKey() + ")").collect(Collectors.joining()));
	}

	private boolean existsSpawnData(int npcId) {
		if (DataManager.SPAWNS_DATA.containsAnySpawnForNpc(npcId))
			return true;
		if (DataManager.TOWN_SPAWNS_DATA.containsAnySpawnForNpc(npcId))
			return true;
		if (DataManager.EVENT_DATA.containsAnySpawnForNpc(npcId))
			return true;
		return false;
	}

	/**
	 * @param questId
	 * @param npcId
	 * @return True, if alternative npc ids, which are valid for this quest, appear in spawn templates (e.g. mobs for quest kills or talk npcs)
	 */
	private boolean existsSpawnDataForAnyAlternativeNpc(int questId, int npcId) {
		XMLQuest quest = DataManager.XML_QUESTS.getQuest(questId);
		if (quest == null)
			return true; // no way to get alternative npcs from non-xml based handlers, so assume the quest spawns work (lol)
		Set<Integer> alternativeNpcs = quest.getAlternativeNpcs(npcId);
		if (alternativeNpcs == null)
			return false;
		return alternativeNpcs.stream().anyMatch(this::existsSpawnData);
	}

	private void addMessageSendingTask() {
		messageTask = CronService.getInstance().schedule(() -> {
			World.getInstance().forEachPlayer(player -> {
				boolean daily = false, weekly = false;
				for (QuestState qs : player.getQuestStateList().getCompletedQuests()) {
					if (qs.isStartable()) {
						QuestTemplate template = DataManager.QUEST_DATA.getQuestById(qs.getQuestId());
						if (!daily && template.isDaily())
							daily = true;
						else if (!weekly && template.isWeekly())
							weekly = true;
						if (daily && weekly)
							break;
					}
				}
				if (daily)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_QUEST_LIMIT_RESET_DAILY());
				if (weekly)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_QUEST_LIMIT_RESET_WEEK());
				if (daily || weekly)
					player.getController().updateNearbyQuests();
				player.getNpcFactions().sendDailyQuest();
			});
		}, "0 0 9 ? * *");
	}

	public static QuestEngine getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final QuestEngine instance = new QuestEngine();
	}
}
