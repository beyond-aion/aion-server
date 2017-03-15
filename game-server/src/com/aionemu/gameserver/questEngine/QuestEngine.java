package com.aionemu.gameserver.questEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.AggregatedClassListener;
import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.scripting.scriptmanager.ScriptManager;
import com.aionemu.gameserver.GameServerError;
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
import com.aionemu.gameserver.model.templates.quest.HandlerSideDrop;
import com.aionemu.gameserver.model.templates.quest.InventoryItem;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.quest.QuestDrop;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.quest.QuestNpc;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_COMPLETED_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.QuestHandlerLoader;
import com.aionemu.gameserver.questEngine.handlers.models.XMLQuest;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.collections.ListSplitter;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneName;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;

/**
 * @author MrPoke, Hilgert
 * @modified vlog, Neon
 */
public class QuestEngine implements GameEngine {

	private static final Logger log = LoggerFactory.getLogger(QuestEngine.class);
	private ScriptManager scriptManager = new ScriptManager();
	private Future<?> messageTask;
	private TIntObjectHashMap<QuestHandler> questHandlers = new TIntObjectHashMap<>();
	private TIntObjectHashMap<QuestNpc> questNpcs = new TIntObjectHashMap<>();
	private TIntObjectHashMap<TIntArrayList> questItemRelated = new TIntObjectHashMap<>();
	private TIntArrayList questHouseItems = new TIntArrayList();
	private TIntObjectHashMap<TIntArrayList> questItems = new TIntObjectHashMap<>();
	private TIntArrayList questOnCompleted = new TIntArrayList();
	private Map<Race, TIntArrayList> questOnLevelUp = new EnumMap<>(Race.class);
	private TIntArrayList questOnDie = new TIntArrayList();
	private TIntArrayList questOnLogOut = new TIntArrayList();
	private TIntArrayList questOnEnterWorld = new TIntArrayList();
	private Map<ZoneName, TIntArrayList> questOnEnterZone = new HashMap<>();
	private Map<ZoneName, TIntArrayList> questOnLeaveZone = new HashMap<>();
	private Map<String, TIntArrayList> questOnPassFlyingRings = new HashMap<>();
	private TIntObjectHashMap<TIntArrayList> questOnMovieEnd = new TIntObjectHashMap<>();
	private List<Integer> questOnTimerEnd = new ArrayList<>();
	private List<Integer> onInvisibleTimerEnd = new ArrayList<>();
	private Map<AbyssRankEnum, TIntArrayList> questOnKillRanked = new EnumMap<>(AbyssRankEnum.class);
	private TIntObjectHashMap<TIntArrayList> questOnKillInWorld = new TIntObjectHashMap<>();
	private TIntObjectHashMap<TIntArrayList> questOnUseSkill = new TIntObjectHashMap<>();
	private TIntIntHashMap questOnFailCraft = new TIntIntHashMap();
	private TIntObjectHashMap<Set<Integer>> questOnEquipItem = new TIntObjectHashMap<>();
	private TIntObjectHashMap<TIntArrayList> questCanAct = new TIntObjectHashMap<>();
	private List<Integer> questOnDredgionReward = new ArrayList<>();
	private Map<BonusType, TIntArrayList> questOnBonusApply = new EnumMap<>(BonusType.class);
	private TIntArrayList questUpdateItems = new TIntArrayList();
	private TIntArrayList reachTarget = new TIntArrayList();
	private TIntArrayList lostTarget = new TIntArrayList();
	private TIntArrayList questOnEnterWindStream = new TIntArrayList();
	private TIntArrayList questRideAction = new TIntArrayList();
	private Map<String, TIntArrayList> questOnKillInZone = new HashMap<>();

	private QuestEngine() {
	}

	@Override
	public void load(CountDownLatch progressLatch) {
		log.info("Quest engine load started");

		for (QuestTemplate data : DataManager.QUEST_DATA.getQuestsData()) {
			for (QuestDrop drop : data.getQuestDrop()) {
				drop.setQuestId(data.getId());
				QuestService.addQuestDrop(drop.getNpcId(), drop);
			}
			if (data.getInventoryItems() != null) {
				for (InventoryItem inventoryItem : data.getInventoryItems().getInventoryItem()) {
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

		try {
			scriptManager.load(new File("./data/scripts/system/quest_handlers.xml"));
			for (XMLQuest xmlQuest : DataManager.XML_QUESTS.getAllQuests())
				xmlQuest.register(this);
			log.info("Loaded " + questHandlers.size() + " quest handlers.");
			if (GSConfig.ANALYZE_QUESTHANDLERS)
				analyzeQuestHandlers();
		} catch (Exception e) {
			throw new GameServerError("Can't initialize quest handlers.", e);
		} finally {
			if (progressLatch != null)
				progressLatch.countDown();
		}

		addMessageSendingTask();
	}

	public void reload() {
		shutdown();
		load(null);
	}

	@Override
	public void shutdown() {
		log.info("Quest engine shutdown started");
		scriptManager.shutdown();
		clear();
		log.info("Quest engine shutdown complete");
	}

	public void clear() {
		messageTask.cancel(false);
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
		questOnMovieEnd.clear();
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
			QuestHandler questHandler = null;
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
				QuestHandler questHandler = getQuestHandlerByQuestId(questId);
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
				QuestHandler questHandler = getQuestHandlerByQuestId(questId);
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
		ListSplitter<QuestState> splittedQs = new ListSplitter<>(player.getQuestStateList().getCompletedQuests(), 1345, true);
		while (splittedQs.hasMore()) {
			int updateMode = splittedQs.isFirst() ? 0 : 1; // first packet sent resets players list
			PacketSendUtility.sendPacket(player, new SM_QUEST_COMPLETED_LIST(updateMode, splittedQs.getNext()));
		}
	}

	/**
	 * Notifies all quest handlers (which registered the event), that the player level changed
	 * 
	 * @param player
	 *          - The player who leveled up
	 */
	public void onLevelChanged(Player player) {
		try {
			TIntArrayList raceQuestsOnLevelUp = getOrCreateOnLevelUpForRace(player.getRace());
			for (int index = 0; index < raceQuestsOnLevelUp.size(); index++) {
				int questId = raceQuestsOnLevelUp.get(index);
				QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs == null || qs.getStatus() != QuestStatus.COMPLETE) {
					QuestHandler questHandler = getQuestHandlerByQuestId(questId);
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
			for (int index = 0; index < questOnCompleted.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(questOnCompleted.get(index));
				if (questHandler != null)
					questHandler.onQuestCompletedEvent(env);
			}
		} catch (Exception ex) {
			log.error("QE: exception in onQuestCompleted", ex);
		}
	}

	public void onDie(QuestEnv env) {
		try {
			for (int index = 0; index < questOnDie.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(questOnDie.get(index));
				if (questHandler != null) {
					env.setQuestId(questOnDie.get(index));
					questHandler.onDieEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onDie", ex);
		}
	}

	public void onLogOut(QuestEnv env) {
		try {
			for (int index = 0; index < questOnLogOut.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(questOnLogOut.get(index));
				if (questHandler != null) {
					env.setQuestId(questOnLogOut.get(index));
					questHandler.onLogOutEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onLogOut", ex);
		}
	}

	public void onNpcReachTarget(QuestEnv env) {
		try {
			for (int index = 0; index < reachTarget.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(reachTarget.get(index));
				if (questHandler != null) {
					env.setQuestId(reachTarget.get(index));
					questHandler.onNpcReachTargetEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onNpcReachTarget", ex);
		}
	}

	public void onNpcLostTarget(QuestEnv env) {
		try {
			for (int index = 0; index < lostTarget.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(lostTarget.get(index));
				if (questHandler != null) {
					env.setQuestId(lostTarget.get(index));
					questHandler.onNpcLostTargetEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onNpcLostTarget", ex);
		}
	}

	public void onPassFlyingRing(QuestEnv env, String FlyRing) {
		try {
			TIntArrayList lists = getOnPassFlyingRingsQuests(FlyRing);
			for (int index = 0; index < lists.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(lists.get(index));
				if (questHandler != null) {
					env.setQuestId(lists.get(index));
					questHandler.onPassFlyingRingEvent(env, FlyRing);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onFlyRingPassEvent", ex);
		}
	}

	public void onEnterWorld(Player player) {
		try {
			for (int index = 0; index < questOnEnterWorld.size(); index++) {
				int questId = questOnEnterWorld.get(index);
				QuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null)
					questHandler.onEnterWorldEvent(new QuestEnv(null, player, questId));
			}
		} catch (Exception ex) {
			log.error("QE: exception in onEnterWorld", ex);
		}
	}

	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		try {
			TIntArrayList lists = getItemRelatedQuests(item.getItemTemplate().getTemplateId());
			for (int index = 0; index < lists.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(lists.get(index));
				if (questHandler != null) {
					env.setQuestId(lists.get(index));
					HandlerResult result = questHandler.onItemUseEvent(env, item);
					// allow other quests to process, the same item can be used in multiple quests
					if (result != HandlerResult.UNKNOWN)
						return result;
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
			for (int index = 0; index < questHouseItems.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(questHouseItems.get(index));
				if (questHandler != null) {
					env.setQuestId(questHouseItems.get(index));
					questHandler.onHouseItemUseEvent(env);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onHouseItemUseEvent", ex);
		}
	}

	public void onItemGet(Player player, int itemId) {
		if (questItems.containsKey(itemId)) {
			for (int i = 0; i < questItems.get(itemId).size(); i++) {
				int questId = questItems.get(itemId).get(i);
				QuestHandler questHandler = getQuestHandlerByQuestId(questId);
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
				TIntArrayList questList = getOnKillRankedQuests(playerRank);
				for (int index = 0; index < questList.size(); index++) {
					int id = questList.get(index);
					QuestHandler questHandler = getQuestHandlerByQuestId(id);
					if (questHandler != null) {
						env.setQuestId(id);
						questHandler.onKillRankedEvent(env);
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
			if (questOnKillInWorld.containsKey(worldId)) {
				TIntArrayList killInWorldQuests = questOnKillInWorld.get(worldId);
				for (int i = 0; i < killInWorldQuests.size(); i++) {
					QuestHandler questHandler = getQuestHandlerByQuestId(killInWorldQuests.get(i));
					if (questHandler != null) {
						env.setQuestId(killInWorldQuests.get(i));
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
			if (questOnKillInZone.containsKey(zoneName)) {
				TIntArrayList killInZoneQuests = getOnKillInZoneQuests(zoneName);
				for (int i = 0; i < killInZoneQuests.size(); i++) {
					QuestHandler questHandler = getQuestHandlerByQuestId(killInZoneQuests.get(i));
					if (questHandler != null) {
						env.setQuestId(killInZoneQuests.get(i));
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
			TIntArrayList lists = getOnEnterZoneQuests(zoneName);
			for (int index = 0; index < lists.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(lists.get(index));
				if (questHandler != null) {
					env.setQuestId(lists.get(index));
					questHandler.onEnterZoneEvent(env, zoneName);
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
			if (questOnLeaveZone.containsKey(zoneName)) {
				TIntArrayList leaveZoneList = questOnLeaveZone.get(zoneName);
				for (int i = 0; i < leaveZoneList.size(); i++) {
					QuestHandler questHandler = getQuestHandlerByQuestId(leaveZoneList.get(i));
					if (questHandler != null) {
						env.setQuestId(leaveZoneList.get(i));
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

	public boolean onMovieEnd(QuestEnv env, int movieId) {
		try {
			TIntArrayList onMovieEndQuests = getOnMovieEndQuests(movieId);
			for (int index = 0; index < onMovieEndQuests.size(); index++) {
				env.setQuestId(onMovieEndQuests.get(index));
				QuestHandler questHandler = getQuestHandlerByQuestId(env.getQuestId());
				if (questHandler != null)
					if (questHandler.onMovieEndEvent(env, movieId))
						return true;
			}
		} catch (Exception ex) {
			log.error("QE: exception in onMovieEnd", ex);
		}
		return false;
	}

	public void onQuestTimerEnd(QuestEnv env) {
		for (int questId : questOnTimerEnd) {
			QuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if (questHandler != null) {
				env.setQuestId(questId);
				questHandler.onQuestTimerEndEvent(env);
			}
		}
	}

	public void onInvisibleTimerEnd(QuestEnv env) {
		for (int questId : onInvisibleTimerEnd) {
			QuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if (questHandler != null) {
				env.setQuestId(questId);
				questHandler.onInvisibleTimerEndEvent(env);
			}
		}
	}

	public boolean onUseSkill(QuestEnv env, int skillId) {
		try {
			if (questOnUseSkill.containsKey(skillId)) {
				TIntArrayList quests = questOnUseSkill.get(skillId);
				for (int i = 0; i < quests.size(); i++) {
					QuestHandler questHandler = getQuestHandlerByQuestId(quests.get(i));
					if (questHandler != null) {
						env.setQuestId(quests.get(i));
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
		if (questOnFailCraft.containsKey(itemId)) {
			int questId = questOnFailCraft.get(itemId);
			QuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if (questHandler != null) {
				if (env.getPlayer().getInventory().getItemCountByItemId(itemId) == 0) {
					env.setQuestId(questId);
					questHandler.onFailCraftEvent(env, itemId);
				}
			}
		}
	}

	public void onEquipItem(QuestEnv env, int itemId) {
		if (questOnEquipItem.containsKey(itemId)) {
			Set<Integer> questIds = questOnEquipItem.get(itemId);
			for (int questId : questIds) {
				QuestHandler questHandler = getQuestHandlerByQuestId(questId);
				if (questHandler != null) {
					env.setQuestId(questId);
					questHandler.onEquipItemEvent(env, itemId);
				}
			}
		}
	}

	public boolean onCanAct(final QuestEnv env, int templateId, final QuestActionType questActionType, final Object... objects) {
		if (questCanAct.containsKey(templateId)) {
			TIntArrayList questIds = questCanAct.get(templateId);
			return !questIds.forEach(new TIntProcedure() {

				@Override
				public boolean execute(int value) {
					QuestHandler questHandler = getQuestHandlerByQuestId(value);
					if (questHandler != null) {
						env.setQuestId(value);
						if (questHandler.onCanAct(env, questActionType, objects))
							return false; // Abort for
					}
					return true;
				}
			});
		}
		return false;
	}

	public void onDredgionReward(QuestEnv env) {
		for (int questId : questOnDredgionReward) {
			QuestHandler questHandler = getQuestHandlerByQuestId(questId);
			if (questHandler != null) {
				env.setQuestId(questId);
				questHandler.onDredgionRewardEvent(env);
			}
		}
	}

	public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
		try {
			TIntArrayList lists = this.getOnBonusApplyQuests(bonusType);
			for (int index = 0; index < lists.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(lists.get(index));
				if (questHandler != null) {
					env.setQuestId(lists.get(index));
					return questHandler.onBonusApplyEvent(env, bonusType, rewardItems);
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
				QuestHandler questHandler = getQuestHandlerByQuestId(questId);
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
		QuestNpc questNpc = null;
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
				QuestHandler questHandler = getQuestHandlerByQuestId(questId);
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
			for (int index = 0; index < questOnEnterWindStream.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(questOnEnterWindStream.get(index));
				if (questHandler != null) {
					env.setQuestId(questOnEnterWindStream.get(index));
					questHandler.onEnterWindStreamEvent(env, loc);
				}
			}
		} catch (Exception ex) {
			log.error("QE: exception in onWindStream", ex);
		}
	}

	public void rideAction(QuestEnv env, int itemId) {
		try {
			for (int index = 0; index < questRideAction.size(); index++) {
				QuestHandler questHandler = getQuestHandlerByQuestId(questRideAction.get(index));
				if (questHandler != null) {
					env.setQuestId(questRideAction.get(index));
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
		if (!questItemRelated.containsKey(itemId)) {
			TIntArrayList itemRelatedQuests = new TIntArrayList();
			itemRelatedQuests.add(questId);
			questItemRelated.put(itemId, itemRelatedQuests);
		} else {
			questItemRelated.get(itemId).add(questId);
		}
	}

	public void registerQuestHouseItem(int questId) {
		if (!questHouseItems.contains(questId))
			questHouseItems.add(questId);
	}

	public void registerGetingItem(int itemId, int questId) {
		if (!questItems.containsKey(itemId)) {
			TIntArrayList questItemsToReg = new TIntArrayList();
			questItemsToReg.add(questId);
			questItems.put(itemId, questItemsToReg);
		} else {
			questItems.get(itemId).add(questId);
		}
	}

	public void registerOnLevelChanged(int questId) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		Race racePermitted = template.getRacePermitted();
		TIntArrayList quests = null;
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

	private TIntArrayList getOrCreateOnLevelUpForRace(Race race) {
		TIntArrayList quests = questOnLevelUp.get(race);
		if (quests == null) {
			quests = new TIntArrayList();
			questOnLevelUp.put(race, quests);
		}
		return quests;
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
		if (!questOnEnterZone.containsKey(zoneName)) {
			TIntArrayList onEnterZoneQuests = new TIntArrayList();
			onEnterZoneQuests.add(questId);
			questOnEnterZone.put(zoneName, onEnterZoneQuests);
		} else {
			questOnEnterZone.get(zoneName).add(questId);
		}
	}

	public void registerOnKillInZone(String zone, int questId) {
		if (!questOnKillInZone.containsKey(zone)) {
			TIntArrayList onKillInZoneQuests = new TIntArrayList();
			onKillInZoneQuests.add(questId);
			questOnKillInZone.put(zone, onKillInZoneQuests);
		} else {
			questOnKillInZone.get(zone).add(questId);
		}
	}

	public void registerOnLeaveZone(ZoneName zoneName, int questId) {
		if (!questOnLeaveZone.containsKey(zoneName)) {
			TIntArrayList onLeaveZoneQuests = new TIntArrayList();
			onLeaveZoneQuests.add(questId);
			questOnLeaveZone.put(zoneName, onLeaveZoneQuests);
		} else {
			questOnLeaveZone.get(zoneName).add(questId);
		}
	}

	public void registerOnKillRanked(AbyssRankEnum playerRank, int questId) {
		for (int rank = playerRank.getId(); rank < 19; rank++) {
			if (!questOnKillRanked.containsKey(AbyssRankEnum.getRankById(rank))) {
				TIntArrayList onKillRankedQuests = new TIntArrayList();
				onKillRankedQuests.add(questId);
				questOnKillRanked.put(AbyssRankEnum.getRankById(rank), onKillRankedQuests);
			} else {
				questOnKillRanked.get(AbyssRankEnum.getRankById(rank)).add(questId);
			}
		}
	}

	public void registerOnKillInWorld(int worldId, int questId) {
		if (!questOnKillInWorld.containsKey(worldId)) {
			TIntArrayList killInWorldQuests = new TIntArrayList();
			killInWorldQuests.add(questId);
			questOnKillInWorld.put(worldId, killInWorldQuests);
		} else {
			questOnKillInWorld.get(worldId).add(questId);
		}
	}

	public void registerOnPassFlyingRings(String flyingRing, int questId) {
		if (!questOnPassFlyingRings.containsKey(flyingRing)) {
			TIntArrayList onPassFlyingRingsQuests = new TIntArrayList();
			onPassFlyingRingsQuests.add(questId);
			questOnPassFlyingRings.put(flyingRing, onPassFlyingRingsQuests);
		} else {
			questOnPassFlyingRings.get(flyingRing).add(questId);
		}
	}

	public void registerOnMovieEndQuest(int moveId, int questId) {
		if (!questOnMovieEnd.containsKey(moveId)) {
			TIntArrayList onMovieEndQuests = new TIntArrayList();
			onMovieEndQuests.add(questId);
			questOnMovieEnd.put(moveId, onMovieEndQuests);
		} else {
			questOnMovieEnd.get(moveId).add(questId);
		}
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
		if (!questOnUseSkill.containsKey(skillId)) {
			TIntArrayList questSkills = new TIntArrayList();
			questSkills.add(questId);
			questOnUseSkill.put(skillId, questSkills);
		} else {
			questOnUseSkill.get(skillId).add(questId);
		}
	}

	public void registerOnFailCraft(int itemId, int questId) {
		if (!questOnFailCraft.containsKey(itemId)) {
			questOnFailCraft.put(itemId, questId);
		}
	}

	public void registerOnEquipItem(int itemId, int questId) {
		if (!questOnEquipItem.containsKey(itemId)) {
			Set<Integer> questIds = new HashSet<>();
			questIds.add(questId);
			questOnEquipItem.put(itemId, questIds);
		} else {
			questOnEquipItem.get(itemId).add(questId);
		}
	}

	public boolean registerCanAct(int questId, int npcId) {
		NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(npcId);
		if (template == null) {
			log.warn("[QuestEngine] No such NPC template for " + npcId + " in Q" + questId);
			return false;
		}
		if ("quest_use_item".equals(template.getAi())) {
			registerCanAct(questId, template);
			return true;
		}
		return false;
	}

	private void registerCanAct(int questId, @Nonnull NpcTemplate template) {
		if (!questCanAct.containsKey(template.getTemplateId())) {
			TIntArrayList questNpcs = new TIntArrayList();
			questNpcs.add(questId);
			questCanAct.put(template.getTemplateId(), questNpcs);
		} else {
			questCanAct.get(template.getTemplateId()).add(questId);
		}
	}

	public void registerOnDredgionReward(int questId) {
		if (!questOnDredgionReward.contains(questId)) {
			questOnDredgionReward.add(questId);
		}
	}

	public void registerOnBonusApply(int questId, BonusType bonusType) {
		if (!questOnBonusApply.containsKey(bonusType)) {
			TIntArrayList onBonusApplyQuests = new TIntArrayList();
			onBonusApplyQuests.add(questId);
			questOnBonusApply.put(bonusType, onBonusApplyQuests);
		} else {
			questOnBonusApply.get(bonusType).add(questId);
		}
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

	private TIntArrayList getOnBonusApplyQuests(BonusType bonusType) {
		if (questOnBonusApply.containsKey(bonusType)) {
			return questOnBonusApply.get(bonusType);
		}
		return new TIntArrayList();
	}

	public QuestNpc getQuestNpc(int npcId) {
		if (questNpcs.containsKey(npcId)) {
			return questNpcs.get(npcId);
		}
		return new QuestNpc(npcId);
	}

	private TIntArrayList getItemRelatedQuests(int itemId) {
		if (questItemRelated.containsKey(itemId)) {
			return questItemRelated.get(itemId);
		}
		return new TIntArrayList();
	}

	private TIntArrayList getOnEnterZoneQuests(ZoneName zoneName) {
		if (questOnEnterZone.containsKey(zoneName)) {
			return questOnEnterZone.get(zoneName);
		}
		return new TIntArrayList();
	}

	private TIntArrayList getOnKillRankedQuests(AbyssRankEnum playerRank) {
		if (questOnKillRanked.containsKey(playerRank)) {
			return questOnKillRanked.get(playerRank);
		}
		return new TIntArrayList();
	}

	private TIntArrayList getOnPassFlyingRingsQuests(String flyingRing) {
		if (questOnPassFlyingRings.containsKey(flyingRing)) {
			return questOnPassFlyingRings.get(flyingRing);
		}
		return new TIntArrayList();
	}

	private TIntArrayList getOnMovieEndQuests(int moveId) {
		if (questOnMovieEnd.containsKey(moveId)) {
			return questOnMovieEnd.get(moveId);
		}
		return new TIntArrayList();
	}

	private TIntArrayList getOnKillInZoneQuests(String zoneName) {
		if (questOnKillInZone.containsKey(zoneName)) {
			return questOnKillInZone.get(zoneName);
		}
		return new TIntArrayList();
	}

	private QuestHandler getQuestHandlerByQuestId(int questId) {
		return questHandlers.get(questId);
	}

	public int getQuestHandlerCount() {
		return questHandlers.size();
	}

	public boolean isHaveHandler(int questId) {
		return questHandlers.containsKey(questId);
	}

	public void addQuestHandler(QuestHandler questHandler) {
		int questId = questHandler.getQuestId();
		if (questHandlers.containsKey(questId)) {
			log.warn("Duplicate quest: " + questId);
		}
		questHandlers.put(questId, questHandler);
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
		log.info("Analyzing quest handlers...");
		Set<Integer> unobtainableQuests = new HashSet<>();
		Set<Integer> factionIds = new HashSet<>();
		for (NpcFactionTemplate nft : DataManager.NPC_FACTIONS_DATA.getNpcFactionsData()) {
			if (nft.getNpcIds() == null || nft.getNpcIds().stream().anyMatch(npcId -> existsSpawnData(npcId)))
				factionIds.add(nft.getId());
		}
		StringBuilder obsoleteHandlers = new StringBuilder();
		for (QuestHandler qh : questHandlers.valueCollection()) {
			QuestTemplate qt = DataManager.QUEST_DATA.getQuestById(qh.getQuestId());
			if (qt.getMinlevelPermitted() == 99) {
				obsoleteHandlers.append("\n\tQuest ").append(qh.getQuestId()).append(" (minLvl=99, handler=").append(qh.getClass().getName()).append(")");
				unobtainableQuests.add(qh.getQuestId());
			} else if (qt.getNpcFactionId() > 0 && !factionIds.contains(qt.getNpcFactionId())) { // outdated or unimplemented npc faction
				obsoleteHandlers.append("\n\tQuest ").append(qh.getQuestId()).append(" (npcFactionId=").append(qt.getNpcFactionId()).append(", handler=")
					.append(qh.getClass().getName()).append(")");
				unobtainableQuests.add(qh.getQuestId());
			}
		}
		StringBuilder missingSpawns = new StringBuilder();
		for (int npcId : questNpcs.keys()) {
			if (!existsSpawnData(npcId)) { // if the npc doesn't appear in any spawn template (world, instance, base, siege, temporary, event, ...)
				Set<Integer> questIds = getQuestNpc(npcId).findAllRegisteredQuestIds();
				if (questIds.stream().allMatch(id -> unobtainableQuests.contains(id) || existsSpawnDataForAnyAlternativeNpc(id, npcId)))
					continue; // don't log unobtainable quests or if alternative npcs appear in spawn data (many quests support outdated + current npcs)
				missingSpawns.append("\n\tNpc ").append(npcId).append(" (quests: ").append(StringUtils.join(questIds, ", ")).append(")");
			}
		}
		if (obsoleteHandlers.length() > 0)
			log.warn("Possibly obsolete quest handlers (quests are not obtainable):{}", obsoleteHandlers.toString());
		if (missingSpawns.length() > 0)
			log.warn("Missing quest npc spawns:{}", missingSpawns.toString());
		if (obsoleteHandlers.length() == 0 && missingSpawns.length() == 0)
			log.info("Quest handler analysis finished without errors!");
		else
			log.info("Quest handler analysis finished (see above log messages for found errors)");
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
		return alternativeNpcs.stream().anyMatch(npc -> existsSpawnData(npc));
	}

	private void addMessageSendingTask() {
		Calendar sendingDate = Calendar.getInstance();
		sendingDate.set(Calendar.AM_PM, Calendar.AM);
		sendingDate.set(Calendar.HOUR, 9);
		sendingDate.set(Calendar.MINUTE, 0);
		sendingDate.set(Calendar.SECOND, 0); // current date 09:00
		if (sendingDate.getTime().getTime() < System.currentTimeMillis()) {
			sendingDate.add(Calendar.HOUR, 24); // next day 09:00
		}
		messageTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				for (Player player : World.getInstance().getAllPlayers()) {
					boolean daily = false, weekly = false;
					for (QuestState qs : player.getQuestStateList().getCompletedQuests()) {
						if (qs.canRepeat()) {
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
				}
			}
		}, sendingDate.getTimeInMillis() - System.currentTimeMillis(), 1000 * 60 * 60 * 24);
	}

	public static final QuestEngine getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final QuestEngine instance = new QuestEngine();
	}
}
