package com.aionemu.gameserver.questEngine.handlers;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.*;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.quest.*;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.questEngine.task.QuestTasks;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke, vlog, Majka
 */
public abstract class AbstractQuestHandler {

	protected static final QuestEngine qe = QuestEngine.getInstance();
	protected final int questId;
	protected List<QuestItems> workItems;
	protected Set<Integer> actionItems;

	/** Create a new AbstractQuestHandler object */
	protected AbstractQuestHandler(int questId) {
		this.questId = questId;
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		if (template != null) { // Some artificial quests have dummy questIds
			loadWorkItems(template);
			loadActionItems(template);
		}
	}

	private void loadWorkItems(QuestTemplate template) {
		if (template.getQuestWorkItems() != null)
			workItems = template.getQuestWorkItems().getQuestWorkItem();
	}

	private void loadActionItems(QuestTemplate template) {
		for (QuestDrop drop : template.getQuestDrop()) {
			if (drop.getNpcId() / 100000 != 7)
				continue;
			if (actionItems == null)
				actionItems = new HashSet<>();
			actionItems.add(drop.getNpcId());
		}
	}

	public Set<Integer> getActionItems() {
		if (actionItems == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(actionItems);
	}

	public final int getQuestId() {
		return questId;
	}

	public abstract void register();

	public boolean onDialogEvent(QuestEnv env) {
		int dialogActionId = env.getDialogActionId();
		if (dialogActionId >= SELECT1 && dialogActionId <= SELECT15_4_4_4_4) {
			// simple "next page" event (action ID = next dialog page ID), but there are some quests where this default behavior does not apply (e.g.
			// 4074)
			sendDialogPacket(env, dialogActionId, questId);
			return true;
		}
		switch (dialogActionId) {
			case ASK_QUEST_ACCEPT: // show quest accept dialog (coming from pre-conversation)
				sendDialogPacket(env, DialogPage.ASK_QUEST_ACCEPT_WINDOW.id(), questId);
				return true;
			case QUEST_ACCEPT_1:
				return env.getVisibleObject() instanceof Npc ? sendQuestDialog(env, 1003) : closeDialogWindow(env);
			case QUEST_REFUSE:
			case QUEST_REFUSE_SIMPLE:
			case QUEST_REFUSE_1:
				return env.getVisibleObject() instanceof Npc ? sendQuestDialog(env, 1004) : closeDialogWindow(env);
			case QUEST_REFUSE_2:
				return env.getVisibleObject() instanceof Npc ? sendQuestDialog(env, 1005) : closeDialogWindow(env);
			case QUEST_REFUSE_3:
				return env.getVisibleObject() instanceof Npc ? sendQuestDialog(env, 1006) : closeDialogWindow(env);
			case QUEST_REFUSE_4:
				return env.getVisibleObject() instanceof Npc ? sendQuestDialog(env, 1007) : closeDialogWindow(env);
			case FINISH_DIALOG: // clicking X or ^ in quest accept window (client closes the window by itself / returns to quest selection)
				return true;
		}
		return false;
	}

	/**
	 * This method is called on every handler (which registered the event), after a player entered a map.
	 */
	public boolean onEnterWorldEvent(QuestEnv env) {
		return false;
	}

	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		return false;
	}

	public boolean onLeaveZoneEvent(QuestEnv env, ZoneName zoneName) {
		return false;
	}

	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		return HandlerResult.UNKNOWN;
	}

	public boolean onHouseItemUseEvent(QuestEnv env) {
		return false;
	}

	public boolean onGetItemEvent(QuestEnv env) {
		return false;
	}

	public boolean onUseSkillEvent(QuestEnv env, int skillId) {
		return false;
	}

	public boolean onKillEvent(QuestEnv env) {
		return false;
	}

	public boolean onAttackEvent(QuestEnv env) {
		return false;
	}

	/**
	 * This method is called on every handler (which registered the event), after a player leveled up or down.
	 * 
	 * @param player
	 *          - The player whose level changed
	 */
	public void onLevelChangedEvent(Player player) {
	}

	/**
	 * This method is called on every handler (which registered the event), after a quest completed.
	 * 
	 * @param env
	 *          - QuestEnv containing the player and the quest ID he completed
	 */
	public void onQuestCompletedEvent(QuestEnv env) {
	}

	public boolean onDieEvent(QuestEnv env) {
		return false;
	}

	public boolean onLogOutEvent(QuestEnv env) {
		return false;
	}

	public boolean onNpcReachTargetEvent(QuestEnv env) {
		return false;
	}

	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return false;
	}

	public void onMovieEndEvent(QuestEnv env, int movieId) {
	}

	public boolean onQuestTimerEndEvent(QuestEnv env) {
		return false;
	}

	public boolean onInvisibleTimerEndEvent(QuestEnv env) {
		return false;
	}

	public boolean onPassFlyingRingEvent(QuestEnv env, String flyingRing) {
		return false;
	}

	public boolean onKillRankedEvent(QuestEnv env) {
		return false;
	}

	public boolean onKillInWorldEvent(QuestEnv env) {
		return false;
	}

	public boolean onKillInZoneEvent(QuestEnv env) {
		return false;
	}

	public boolean onFailCraftEvent(QuestEnv env, int itemId) {
		return false;
	}

	public boolean onEquipItemEvent(QuestEnv env, int itemId) {
		return false;
	}

	public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(env.getQuestId());
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		if (questEventType == QuestActionType.ACTION_ITEM_USE && actionItems != null) {
			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(env.getQuestId());
			int droppedItem = 0;
			int dropCount = 0;
			for (QuestDrop drop : template.getQuestDrop()) {
				if (drop.getNpcId() == env.getTargetId()) {
					droppedItem = drop.getItemId();
					break;
				}
			}
			CollectItems collectItems = template.getCollectItems();
			if (collectItems != null && droppedItem != 0) {
				for (CollectItem item : collectItems.getCollectItem()) {
					if (item.getItemId() == droppedItem) {
						dropCount = item.getCount();
						break;
					}
				}
				if (dropCount != 0) {
					long currentCount = player.getInventory().getItemCountByItemId(droppedItem);
					if (currentCount >= dropCount)
						return false;
				}
			}
		}
		return true;
	}

	public boolean onAddAggroListEvent(QuestEnv env) {
		return false;
	}

	public boolean onAtDistanceEvent(QuestEnv env) {
		return false;
	}

	public boolean onEnterWindStreamEvent(QuestEnv env, int worldId) {
		return false;
	}

	public boolean rideAction(QuestEnv env, int rideItemId) {
		return false;
	}

	public boolean onDredgionRewardEvent(QuestEnv env) {
		return false;
	}

	public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
		return HandlerResult.UNKNOWN;
	}

	public boolean onProtectEndEvent(QuestEnv env) {
		return false;
	}

	public boolean onProtectFailEvent(QuestEnv env) {
		return false;
	}

	/** Update the status of the quest in player's journal */
	public void updateQuestStatus(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.UPDATE, qs));
		if (qs.getStatus() == QuestStatus.COMPLETE || qs.getStatus() == QuestStatus.REWARD)
			player.getController().updateNearbyQuests();
	}

	public void changeQuestStep(QuestEnv env, int oldStep, int newStep) {
		changeQuestStep(env, oldStep, newStep, false, oldStep > 0x3F || newStep > 0x3F ? -1 : 0);
	}

	public void changeQuestStep(QuestEnv env, int step, int nextStep, boolean reward) {
		changeQuestStep(env, step, nextStep, reward, 0);
	}

	/** Change the quest step to the next step or set quest status to reward */
	public void changeQuestStep(QuestEnv env, int step, int nextStep, boolean reward, int varNum) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs != null && (varNum == -1 ? qs.getQuestVars().getQuestVars() == step : qs.getQuestVarById(varNum) == step)) {
			if (nextStep != step) { // quest can be rolled back if nextStep < step
				if (step > nextStep && qs.getStatus() == QuestStatus.START)
					PacketSendUtility.sendPacket(env.getPlayer(),
						SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP(DataManager.QUEST_DATA.getQuestById(questId).getL10n()));
				if (varNum == -1)
					qs.setQuestVar(nextStep);
				else
					qs.setQuestVarById(varNum, nextStep);
			}
			if (reward || nextStep != step) {
				if (reward)
					qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
			}
		}
	}

	/** Send dialog to the player */
	public boolean sendQuestDialog(QuestEnv env, int dialogPageId) {
		if (dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW1.id() || dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW2.id()
			|| dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW3.id() || dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW4.id()
			|| dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW5.id() || dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW6.id()
			|| dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW7.id() || dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW8.id()
			|| dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW9.id() || dialogPageId == DialogPage.SELECT_QUEST_REWARD_WINDOW10.id()) {
			QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
			if (qs == null || qs.getStatus() != QuestStatus.REWARD) // reward packet exploitation fix
				return false;
		}
		// Not using handler questId, because some quests may handle events when quests are finished
		// In that case questId must be zero!!! (Kromede entry for example)
		sendDialogPacket(env, dialogPageId, env.getQuestId());
		return true;
	}

	private void sendDialogPacket(QuestEnv env, int dialogPageId, int questId) {
		int objId = 0;
		if (env.getVisibleObject() != null) {
			objId = env.getVisibleObject().getObjectId();
		}
		PacketSendUtility.sendPacket(env.getPlayer(), new SM_DIALOG_WINDOW(objId, dialogPageId, questId));
	}

	public boolean sendQuestSelectionDialog(QuestEnv env) {
		sendDialogPacket(env, 10, 0);
		return true;
	}

	public boolean closeDialogWindow(QuestEnv env) {
		sendDialogPacket(env, 0, 0);
		return true;
	}

	public boolean sendQuestStartDialog(QuestEnv env) {
		return sendQuestStartDialog(env, 0, 0); // TODO remove all calls and replace with super.onDialogEvent()
	}

	public boolean sendQuestStartDialog(QuestEnv env, QuestItems workItem) {
		return workItem == null ? sendQuestStartDialog(env, 0, 0) : sendQuestStartDialog(env, workItem.getItemId(), workItem.getCount());
	}

	/** Send default start quest dialog and start it (give the item on start) */
	public boolean sendQuestStartDialog(QuestEnv env, int itemId, long itemCount) {
		switch (env.getDialogActionId()) {
			case ASK_QUEST_ACCEPT:
				return sendQuestDialog(env, 4);
			case QUEST_ACCEPT:
			case QUEST_ACCEPT_1:
			case QUEST_ACCEPT_SIMPLE:
				if (QuestService.startQuest(env)) {
					if (itemId != 0 && itemCount != 0)
						giveQuestItem(env, itemId, itemCount);
					if (env.getDialogActionId() != QUEST_ACCEPT_SIMPLE && env.getVisibleObject() instanceof Npc)
						return sendQuestDialog(env, 1003);
					else
						return closeDialogWindow(env);
				}
				break;
			case QUEST_REFUSE_1:
			case QUEST_REFUSE_2:
				return sendQuestDialog(env, 1004);
			case QUEST_REFUSE_SIMPLE:
				return closeDialogWindow(env);
			case FINISH_DIALOG:
				return sendQuestSelectionDialog(env);
		}
		return false;
	}

	/** Remove all quest items and send and finish the quest */
	public boolean sendQuestEndDialog(QuestEnv env, int[] questItemsToRemove) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		QuestStatus status = qs == null ? null : qs.getStatus();
		for (int itemId : questItemsToRemove)
			removeQuestItem(env, itemId, player.getInventory().getItemCountByItemId(itemId), status);
		return sendQuestEndDialog(env);
	}

	/**
	 * Sends reward selection dialog of the quest or finishes it (if selection dialog was active)
	 */
	public boolean sendQuestEndDialog(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.REWARD)
			return false; // reward packet exploitation fix (or buggy quest handler)

		int dialogActionId = env.getDialogActionId();
		if (dialogActionId >= SELECTED_QUEST_REWARD1 && dialogActionId <= SELECTED_QUEST_NOREWARD) {
			if (QuestService.finishQuest(env)) {
				Npc npc = (Npc) env.getVisibleObject();
				QuestNpc questNpc = QuestEngine.getInstance().getQuestNpc(npc.getNpcId());
				boolean npcHasActiveQuest = false;
				for (Integer questId : questNpc.getOnTalkEvent()) { // all quest IDs that have registered talk events for this npc
					QuestState qs2 = player.getQuestStateList().getQuestState(questId);
					if (qs2 != null && qs2.getStatus() == QuestStatus.REWARD) { // TODO make sure that this npc is the end npc
						env.setQuestId(questId);
						env.setDialogActionId(DialogAction.USE_OBJECT); // show default dialog (reward selection for next quest)
						return QuestEngine.getInstance().onDialog(new QuestEnv(npc, player, questId, DialogAction.USE_OBJECT));
					} else if (!npcHasActiveQuest && qs2 != null && qs2.getStatus() == QuestStatus.START) {
						boolean isQuestStartNpc = questNpc.getOnQuestStart().contains(questId);
						if (!isQuestStartNpc || DataManager.QUEST_DATA.getQuestById(questId).isMission() && qs2.getQuestVars().getQuestVars() == 0)
							npcHasActiveQuest = true; // TODO correct way to make sure that active quest can be continued at this npc
					}
				}
				boolean npcHasNewQuest = false;
				for (Integer questId : questNpc.getOnQuestStart()) { // all quest IDs that are registered to be started at this npc
					if (QuestService.checkStartConditions(player, questId, false)) {
						npcHasNewQuest = true;
						QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
						for (XMLStartCondition startCondition : template.getXMLStartConditions()) {
							List<FinishedQuestCond> finishedQuests = startCondition.getFinishedPreconditions();
							if (finishedQuests != null) {
								for (FinishedQuestCond fcondition : finishedQuests) {
									if (fcondition.getQuestId() == env.getQuestId() && isAcceptableQuest(template)) {
										env.setQuestId(questId);
										env.setDialogActionId(DialogAction.QUEST_SELECT);
										env.setDialogContinuationFromPreQuest(true);
										return QuestEngine.getInstance().onDialog(env); // show start dialog of follow-up quest
									}
								}
							}
						}
					}
				}
				return npcHasActiveQuest || npcHasNewQuest ? sendQuestSelectionDialog(env) : closeDialogWindow(env);
			}
		} else {
			switch (dialogActionId) {
				case SET_SUCCEED: // report to pre-end npc (another npc is actually responsible for rewarding, so close this window)
					return closeDialogWindow(env);
				case USE_OBJECT: // start talking to npc
				case SELECT_QUEST_REWARD: // report to end npc
				case CHECK_USER_HAS_QUEST_ITEM: // report to end npc with collect item checks
				case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: // report to end npc with collect item checks
					QuestService.validateAndFixRewardGroup(qs, questId); // fixes the reward group if necessary
					// show reward selection page
					return sendQuestDialog(env, DialogPage.getRewardPageByIndex(qs.getRewardGroup()).id());
			}
		}
		return false;
	}

	private boolean isAcceptableQuest(QuestTemplate quest) {
		if (quest.getMinlevelPermitted() == 99)
			return false;
		if (quest.getRewards().isEmpty() && quest.getExtendedRewards() == null && quest.getBonus() == null && quest.getQuestDrop().isEmpty()
			&& Stream.of(PlayerClass.values()).allMatch(c -> quest.getSelectableRewardByClass(c).isEmpty())) {
			return false;
		}
		return true;
	}

	public boolean defaultCloseDialog(QuestEnv env, int step, int nextStep) {
		return defaultCloseDialog(env, step, nextStep, false, false, 0, 0, 0, 0);
	}

	public boolean defaultCloseDialog(QuestEnv env, int step, int nextStep, int giveItemId, long giveItemCount) {
		return defaultCloseDialog(env, step, nextStep, false, false, giveItemId, giveItemCount, 0, 0);
	}

	public boolean defaultCloseDialog(QuestEnv env, int step, int nextStep, boolean reward, boolean sameNpc) {
		return defaultCloseDialog(env, step, nextStep, reward, sameNpc, 0, 0, 0, 0);
	}

	public boolean defaultCloseDialog(QuestEnv env, int step, int nextStep, boolean reward, boolean sameNpc, QuestItems questItemToAdd) {
		return defaultCloseDialog(env, step, nextStep, reward, sameNpc, questItemToAdd.getItemId(), questItemToAdd.getCount(), 0, 0);
	}

	public boolean defaultCloseDialog(QuestEnv env, int step, int nextStep, int giveItemId, long giveItemCount, int removeItemId,
		long removeItemCount) {
		return defaultCloseDialog(env, step, nextStep, false, false, giveItemId, giveItemCount, removeItemId, removeItemCount);
	}

	/**
	 * Handle on close dialog event, changing the quest status and giving/removing quest items
	 */
	public boolean defaultCloseDialog(QuestEnv env, int step, int nextStep, boolean reward, boolean sameNpc, int giveItemId, long giveItemCount,
		int removeItemId, long removeItemCount) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs.getQuestVarById(0) == step) {
			if (giveItemId != 0 && giveItemCount != 0) {
				if (!giveQuestItem(env, giveItemId, giveItemCount)) {
					return false;
				}
			}
			removeQuestItem(env, removeItemId, removeItemCount, qs.getStatus());
			changeQuestStep(env, step, nextStep, reward);
			if (sameNpc) {
				return sendQuestEndDialog(env);
			}
			if (env.getVisibleObject() instanceof Npc)
				((Npc) env.getVisibleObject()).getAi().onCreatureEvent(AIEventType.DIALOG_FINISH, env.getPlayer());
			return closeDialogWindow(env);
		}
		return false;
	}

	public boolean checkQuestItems(QuestEnv env, int step, int nextStep, boolean reward, int checkOkId, int checkFailId) {
		return checkQuestItems(env, step, nextStep, reward, checkOkId, checkFailId, 0, 0);
	}

	/** Check if the player has quest item, listed in the quest_data.xml in his inventory */
	public boolean checkQuestItems(QuestEnv env, int step, int nextStep, boolean reward, int checkOkId, int checkFailId, int giveItemId,
		int giveItemCount) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs.getQuestVarById(0) == step) {
			if (QuestService.collectItemCheck(env, true)) {
				if (giveItemId != 0 && giveItemCount != 0) {
					if (!giveQuestItem(env, giveItemId, giveItemCount)) {
						return false;
					}
				}
				changeQuestStep(env, step, nextStep, reward);
				return sendQuestDialog(env, checkOkId);
			} else {
				return sendQuestDialog(env, checkFailId);
			}
		}
		return false;
	}

	/** Check if the player has quest item (simple version), listed in the quest_data.xml in his inventory */
	public boolean checkQuestItemsSimple(QuestEnv env, int step, int nextStep, boolean reward, int checkOkId, int giveItemId, int giveItemCount) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs.getQuestVarById(0) == step) {
			if (QuestService.collectItemCheck(env, true)) {
				if (giveItemId != 0 && giveItemCount != 0) {
					if (!giveQuestItem(env, giveItemId, giveItemCount)) {
						return false;
					}
				}
				changeQuestStep(env, step, nextStep, reward);
				return sendQuestDialog(env, checkOkId);
			} else
				return closeDialogWindow(env);
		}
		return false;
	}

	/** To use for checking the items, not listed in the collect_items in the quest_data.xml */
	public boolean checkItemExistence(QuestEnv env, int step, int nextStep, boolean reward, int itemId, int itemCount, boolean remove, int checkOkId,
		int checkFailId, int giveItemId, int giveItemCount) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs.getQuestVarById(0) == step) {
			if (checkItemExistence(env, itemId, itemCount, remove)) {
				if (giveItemId != 0 && giveItemCount != 0) {
					if (!giveQuestItem(env, giveItemId, giveItemCount)) {
						return false;
					}
				}
				changeQuestStep(env, step, nextStep, reward);
				return sendQuestDialog(env, checkOkId);
			} else {
				return sendQuestDialog(env, checkFailId);
			}
		}
		return false;
	}

	/** Check, if item exists in the player's inventory and probably remove it */
	public boolean checkItemExistence(QuestEnv env, int itemId, int itemCount, boolean remove) {
		Player player = env.getPlayer();
		if (player.getInventory().getItemCountByItemId(itemId) >= itemCount) {
			if (remove) {
				if (!removeQuestItem(env, itemId, itemCount)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public void sendEmotion(QuestEnv env, Creature emoteCreature, EmotionId emotion, boolean broadcast) {
		Player player = env.getPlayer();
		int targetId = player.equals(emoteCreature) ? env.getVisibleObject().getObjectId() : player.getObjectId();
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(emoteCreature, EmotionType.EMOTE, emotion.id(), targetId), broadcast);
	}

	/** Give the quest item to player's inventory */
	public boolean giveQuestItem(QuestEnv env, int itemId, long itemCount) {
		return giveQuestItem(env, itemId, itemCount, ItemAddType.QUEST_WORK_ITEM, ItemUpdateType.INC_ITEM_COLLECT);
	}

	public boolean giveQuestItem(QuestEnv env, int itemId, long itemCount, ItemAddType addType) {
		return giveQuestItem(env, itemId, itemCount, addType, ItemUpdateType.INC_ITEM_COLLECT);
	}

	public boolean giveQuestItem(QuestEnv env, int itemId, long itemCount, ItemAddType addType, ItemUpdateType updateType) {
		Player player = env.getPlayer();
		ItemTemplate item = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if (itemId != 0 && itemCount != 0) {
			long existentItemCount = player.getInventory().getItemCountByItemId(itemId);
			if (existentItemCount < itemCount) {
				long itemsToGive = itemCount - existentItemCount; // some quest work items come from multiple quests, don't add again
				ItemService.addItem(player, itemId, itemsToGive, true, new ItemService.ItemUpdatePredicate(addType, updateType));
				return true;
			} else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CAN_NOT_GET_LORE_ITEM((item.getL10n())));
				return true;
			}
		}
		return false;
	}

	/** Remove the specified count of this quest item from player's inventory */
	public boolean removeQuestItem(QuestEnv env, int itemId, long itemCount) {
		Player player = env.getPlayer();
		if (itemId != 0 && itemCount > 0) {
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			return player.getInventory().decreaseByItemId(itemId, itemCount, qs == null ? QuestStatus.START : qs.getStatus());
		}
		return false;
	}

	public boolean removeQuestItem(QuestEnv env, int itemId, long itemCount, QuestStatus questStatus) {
		Player player = env.getPlayer();
		if (itemId != 0 && itemCount != 0) {
			return player.getInventory().decreaseByItemId(itemId, itemCount, questStatus);
		}
		return false;
	}

	public boolean playQuestMovie(QuestEnv env, int movieId) {
		return playQuestMovie(env, movieId, false);
	}

	public boolean playQuestMovie(QuestEnv env, int cutsceneId, boolean isCutsceneMovie) {
		int targetObjectId = env.getVisibleObject() == null ? 0 : env.getVisibleObject().getObjectId();
		PacketSendUtility.sendPacket(env.getPlayer(), new SM_PLAY_MOVIE(isCutsceneMovie, targetObjectId, env.getQuestId(), cutsceneId, true));
		return false;
	}

	/** For single kill */
	public boolean defaultOnKillEvent(QuestEnv env, int npcId, int startVar, int endVar) {
		int[] mobids = { npcId };
		return defaultOnKillEvent(env, mobids, startVar, endVar);
	}

	/** For multiple kills */
	public boolean defaultOnKillEvent(QuestEnv env, int[] npcIds, int startVar, int endVar) {
		return defaultOnKillEvent(env, npcIds, startVar, endVar, 0);
	}

	/** For single kill on another QuestVar */
	public boolean defaultOnKillEvent(QuestEnv env, int npcId, int startVar, int endVar, int varNum) {
		int[] mobids = { npcId };
		return defaultOnKillEvent(env, mobids, startVar, endVar, varNum);
	}

	/** Handle onKill event */
	public boolean defaultOnKillEvent(QuestEnv env, int[] npcIds, int startVar, int endVar, int varNum) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(varNum);
			int targetId = env.getTargetId();
			for (int id : npcIds) {
				if (targetId == id) {
					if (var >= startVar && var < endVar) {
						qs.setQuestVarById(varNum, var + 1);
						updateQuestStatus(env);
						return true;
					}
				}
			}
		}
		return false;
	}

	/** For single kill and reward status after it */
	public boolean defaultOnKillEvent(QuestEnv env, int npcId, int startVar, boolean reward) {
		int[] mobids = { npcId };
		return (defaultOnKillEvent(env, mobids, startVar, reward, 0));
	}

	/** For single kill on another QuestVar and reward status after it */
	public boolean defaultOnKillEvent(QuestEnv env, int npcId, int startVar, boolean reward, int varNum) {
		int[] mobids = { npcId };
		return (defaultOnKillEvent(env, mobids, startVar, reward, varNum));
	}

	/** For multiple kills and reward status after it */
	public boolean defaultOnKillEvent(QuestEnv env, int[] npcIds, int startVar, boolean reward) {
		return (defaultOnKillEvent(env, npcIds, startVar, reward, 0));
	}

	/** Handle onKill event with reward status */
	public boolean defaultOnKillEvent(QuestEnv env, int[] npcIds, int startVar, boolean reward, int varNum) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(varNum);
			int targetId = env.getTargetId();
			for (int id : npcIds) {
				if (targetId == id) {
					if (var == startVar) {
						if (reward) {
							qs.setStatus(QuestStatus.REWARD);
						} else {
							qs.setQuestVarById(varNum, var + 1);
						}
						updateQuestStatus(env);
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean defaultOnKillRankedEvent(QuestEnv env, int startVar, int endVar, boolean reward) {
		return defaultOnKillRankedEvent(env, startVar, endVar, reward, false);
	}

	public boolean defaultOnKillRankedEvent(QuestEnv env, int startVar, int endVar, boolean reward, boolean isDataDriven) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (isDataDriven) {
				int varKill = qs.getQuestVarById(1);
				if (varKill >= startVar && varKill < (endVar - 1)) {
					changeQuestStep(env, varKill, varKill + 1, false, 1);
					return true;
				} else if (varKill == (endVar - 1)) {
					if (reward)
						qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVar(var + 1);
				}
			} else {
				if (var >= startVar && var < (endVar - 1)) {
					changeQuestStep(env, var, var + 1, false);
					return true;
				} else if (var == (endVar - 1)) {
					if (reward)
						qs.setStatus(QuestStatus.REWARD);
					else
						qs.setQuestVarById(0, var + 1);
				}
			}
			updateQuestStatus(env);
			return true;
		}
		return false;
	}

	public boolean defaultOnKillInZoneEvent(QuestEnv env, int startVar, int endVar, boolean reward) {
		return defaultOnKillRankedEvent(env, startVar, endVar, reward, false);
	}

	public boolean defaultOnKillInZoneEvent(QuestEnv env, int startVar, int endVar, boolean reward, boolean isDataDriven) {
		return defaultOnKillRankedEvent(env, startVar, endVar, reward, isDataDriven);
	}

	public boolean defaultOnUseSkillEvent(QuestEnv env, int startVar, int endVar, int varNum) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(varNum);
			if (var >= startVar && var < endVar) {
				changeQuestStep(env, var, var + 1, false, varNum);
				return true;
			}
		}
		return false;
	}

	/** NPC starts following the player to the target. Use onLostTarget and onReachTarget for further actions. */
	public boolean defaultStartFollowEvent(QuestEnv env, Npc follower, int targetNpcId, int step, int nextStep) {
		Player player = env.getPlayer();
		if (!(env.getVisibleObject() instanceof Npc)) {
			return false;
		}
		follower.overrideNpcType(CreatureType.PEACE);
		follower.getAi().onCreatureEvent(AIEventType.FOLLOW_ME, player);
		player.getController().addTask(TaskId.QUEST_FOLLOW, QuestTasks.newFollowingToTargetCheckTask(env, follower, targetNpcId));
		return step == 0 && nextStep == 0 || defaultCloseDialog(env, step, nextStep);
	}

	/** NPC starts following the player to the target location. Use onLostTarget and onReachTarget for further actions. */
	public boolean defaultStartFollowEvent(QuestEnv env, Npc follower, float x, float y, float z, int step, int nextStep) {
		final Player player = env.getPlayer();
		if (!(env.getVisibleObject() instanceof Npc)) {
			return false;
		}
		PacketSendUtility.sendPacket(player, new SM_NPC_INFO(follower, player));
		follower.getAi().onCreatureEvent(AIEventType.FOLLOW_ME, player);
		player.getController().addTask(TaskId.QUEST_FOLLOW, QuestTasks.newFollowingToTargetCheckTask(env, follower, x, y, z));
		if (step == 0 && nextStep == 0) {
			return true;
		} else {
			return defaultCloseDialog(env, step, nextStep);
		}
	}

	/** NPC stops following the player. Used in both onLostTargetEvent and onReachTargetEvent. */
	public boolean defaultFollowEndEvent(QuestEnv env, int step, int nextStep, boolean reward, int movie) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == step) {
				changeQuestStep(env, step, nextStep, reward);
				if (movie != 0)
					playQuestMovie(env, movie);
				return true;
			}
		}
		return false;
	}

	public boolean defaultFollowEndEvent(QuestEnv env, int step, int nextStep, boolean reward) {
		return defaultFollowEndEvent(env, step, nextStep, reward, 0);
	}

	/** Changing quest step on getting item */
	public boolean defaultOnGetItemEvent(QuestEnv env, int step, int nextStep, boolean reward) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == step) {
				changeQuestStep(env, step, nextStep, reward);
				return true;
			}
		}
		return false;
	}

	public boolean useQuestObject(QuestEnv env, int step, int nextStep, boolean reward, boolean die) {
		return useQuestObject(env, step, nextStep, reward, 0, 0, 0, 0, 0, 0, die);
	}

	public boolean useQuestObject(QuestEnv env, int step, int nextStep, boolean reward, int varNum, boolean die) {
		return useQuestObject(env, step, nextStep, reward, varNum, 0, 0, 0, 0, 0, die);
	}

	public boolean useQuestObject(QuestEnv env, int step, int nextStep, boolean reward, int varNum) {
		return useQuestObject(env, step, nextStep, reward, varNum, 0, 0, 0, 0, 0, false);
	}

	public boolean useQuestObject(QuestEnv env, int step, int nextStep, boolean reward, int varNum, int addItemId, int addItemCount) {
		return useQuestObject(env, step, nextStep, reward, varNum, addItemId, addItemCount, 0, 0, 0, false);
	}

	public boolean useQuestObject(QuestEnv env, int step, int nextStep, boolean reward, int varNum, int addItemId, int addItemCount, int removeItemId,
		int removeItemCount) {
		return useQuestObject(env, step, nextStep, reward, varNum, addItemId, addItemCount, removeItemId, removeItemCount, 0, false);
	}

	public boolean useQuestObject(QuestEnv env, int step, int nextStep, boolean reward, int varNum, int movieId) {
		return useQuestObject(env, step, nextStep, reward, varNum, 0, 0, 0, 0, movieId, false);
	}

	/** Handle use object event */
	public boolean useQuestObject(QuestEnv env, int step, int nextStep, boolean reward, int varNum, int addItemId, int addItemCount, int removeItemId,
		int removeItemCount, int movieId, boolean dieObject) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		if (qs.getQuestVarById(varNum) == step) {
			if (addItemId != 0 && addItemCount != 0) {
				if (!giveQuestItem(env, addItemId, addItemCount)) {
					return false;
				}
			}
			if (removeItemId != 0 && removeItemCount != 0) {
				removeQuestItem(env, removeItemId, removeItemCount);
			}
			if (movieId != 0) {
				playQuestMovie(env, movieId);
			}
			if (dieObject) {
				Npc npc = (Npc) player.getTarget();
				if (!env.getVisibleObject().equals(npc))
					return false;
				npc.getController().die(player);
			}
			changeQuestStep(env, step, nextStep, reward, varNum);
			return true;
		}
		return false;
	}

	public boolean useQuestItem(QuestEnv env, Item item, int step, int nextStep, boolean reward) {
		return useQuestItem(env, item, step, nextStep, reward, 0, 0, 0);
	}

	public boolean useQuestItem(QuestEnv env, Item item, int step, int nextStep, boolean reward, final int addItemId, final int addItemCount) {
		return useQuestItem(env, item, step, nextStep, reward, addItemId, addItemCount, 0);
	}

	public boolean useQuestItem(QuestEnv env, Item item, int step, int nextStep, boolean reward, int movieId) {
		return useQuestItem(env, item, step, nextStep, reward, 0, 0, movieId);
	}

	public boolean useQuestItem(final QuestEnv env, final Item item, final int step, final int nextStep, final boolean reward, final int addItemId,
		final int addItemCount, final int movieId) {
		return useQuestItem(env, item, step, nextStep, reward, addItemId, addItemCount, movieId, 0);
	}

	/** Handle use item event */
	public boolean useQuestItem(final QuestEnv env, final Item item, final int step, final int nextStep, final boolean reward, final int addItemId,
		final int addItemCount, final int movieId, final int varNum) {
		final Player player = env.getPlayer();
		if (player == null) {
			return false;
		}
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		final int itemId = item.getItemId();
		final int objectId = item.getObjectId();

		if (qs.getQuestVarById(varNum) == step) {
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), objectId, itemId, 3000, 0, 0), true);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), objectId, itemId, 0, 1, 0), true);
					removeQuestItem(env, itemId, 1);

					if (addItemId != 0 && addItemCount != 0) {
						if (!giveQuestItem(env, addItemId, addItemCount)) {
							return;
						}
					}
					if (movieId != 0) {
						playQuestMovie(env, movieId);
					}
					changeQuestStep(env, step, nextStep, reward, varNum);
				}
			}, 3000);
			return true;
		}
		return false;
	}

	/**
	 * Starts or locks quest on level up (usually used from campaign quest handlers)
	 * 
	 * @param player
	 *          - Player who wants to start the quest
	 * @param preQuests
	 *          - The quests to be completed before starting this one
	 * @return True if successfully started
	 */
	public boolean defaultOnLevelChangedEvent(Player player, int... preQuests) {
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		// Only null or LOCKED quests can be started
		if (qs != null && qs.getStatus() != QuestStatus.LOCKED)
			return false;

		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		int minLvlDiff = template.isMission() ? 2 : 0;
		// Check all player requirements (but allowed diff to quest minLevel = 2)
		if (!QuestService.checkStartConditions(player, questId, false, minLvlDiff, false, false, template.isMission()))
			return false;

		boolean missingRequirement = false;
		for (int id : preQuests) {
			QuestState qs2 = player.getQuestStateList().getQuestState(id);
			if (!missingRequirement && (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)) {
				if (qs != null || !template.isMission()) // fast return if its already locked or no campaign quest
					return false;
				missingRequirement = true;
			}
			if (missingRequirement && qs2 != null && qs2.getStatus() == QuestStatus.COMPLETE) {
				QuestService.addOrUpdateQuest(player, questId, QuestStatus.LOCKED);
				return false;
			}
		}
		if (missingRequirement)
			return false;

		// Check the quests, that have to be done before starting this one and other start conditions, listed in quest_data
		for (XMLStartCondition cond : template.getXMLStartConditions()) {
			if (!cond.check(player, false)) {
				if (qs == null && template.isMission())
					QuestService.addOrUpdateQuest(player, questId, QuestStatus.LOCKED);
				return false;
			}
		}

		// Send locked quest if the player is <= 2 levels below quest min level (as specified in the check above)
		if (minLvlDiff > 0 && player.getLevel() < template.getMinlevelPermitted()) {
			if (qs == null && template.isMission())
				QuestService.addOrUpdateQuest(player, questId, QuestStatus.LOCKED);
			return false;
		}

		// All conditions are met, start the quest
		QuestService.addOrUpdateQuest(player, questId, QuestStatus.START);
		return true;
	}

	/**
	 * Starts or locks quest after quest completion (usually used from campaign quest handlers).
	 * 
	 * @param env
	 *          - QuestEnv containing the player and quest which he completed
	 * @param preQuests
	 *          - The quests to be completed before starting this one
	 * @return True if successfully started
	 */
	public boolean defaultOnQuestCompletedEvent(QuestEnv env, int... preQuests) {
		Player player = env.getPlayer();
		int finishedQuestId = env.getQuestId();
		QuestStateList qsl = player.getQuestStateList();
		QuestState qs = qsl.getQuestState(questId);

		// Only null or LOCKED quests can be started
		if (qs != null && qs.getStatus() != QuestStatus.LOCKED)
			return false;

		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		int minLvlDiff = template.isMission() ? 15 : 0; // this ensures to add all follow-up quests in locked state
		// Check all player requirements first
		if (!QuestService.checkStartConditions(player, questId, false, minLvlDiff, false, false, template.isMission()))
			return false;

		boolean missingRequirement = false;
		boolean hasFinishedPreQuest = false;
		for (int id : preQuests) {
			QuestState qs2 = qsl.getQuestState(id);
			if (!missingRequirement && (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)) {
				if (qs != null || !template.isMission()) // fast return if its already locked or no campaign quest
					return false;
				missingRequirement = true;
			}
			if (finishedQuestId == id)
				hasFinishedPreQuest = true;
			if (missingRequirement && (hasFinishedPreQuest || qs2 != null && qs2.getStatus() == QuestStatus.COMPLETE)) { // if any pre quest is finished
				QuestService.addOrUpdateQuest(player, questId, QuestStatus.LOCKED);
				return false;
			}
		}
		if (missingRequirement)
			return false;

		// Check the quests, that have to be done before starting this one and other start conditions, listed in quest_data
		missingRequirement = false;
		for (XMLStartCondition cond : template.getXMLStartConditions()) {
			if (!cond.check(player, false)) {
				if (qs != null || !template.isMission()) // fast return if its already locked or no campaign quest
					return false;
				else if (hasAnyPreQuestFinished(qsl, cond)) { // recursive check
					QuestService.addOrUpdateQuest(player, questId, QuestStatus.LOCKED);
					return false;
				}
				missingRequirement = true;
			}
		}
		if (missingRequirement)
			return false;

		// Send locked quest if the players level is in the minLvlDiff range (1-15)
		if (minLvlDiff > 0 && player.getLevel() < template.getMinlevelPermitted()) {
			if (qs == null && hasFinishedPreQuest)
				QuestService.addOrUpdateQuest(player, questId, QuestStatus.LOCKED);
			return false;
		}

		// All conditions are met, start the quest
		QuestService.addOrUpdateQuest(player, questId, QuestStatus.START);
		return true;
	}

	/**
	 * Checks recursively if any pre-quest that is required, is completed
	 * 
	 * @param qsl
	 *          - Players {@link QuestStateList}
	 * @param startCondition
	 *          - The XML start condition list
	 * @return True, if any pre-quest of this series is finished
	 */
	private static boolean hasAnyPreQuestFinished(QuestStateList qsl, XMLStartCondition startCondition) {
		List<FinishedQuestCond> finishedQuests = startCondition.getFinishedPreconditions();
		if (finishedQuests != null) {
			for (FinishedQuestCond finishedCond : finishedQuests) {
				QuestState qs = qsl.getQuestState(finishedCond.getQuestId());
				if (qs != null && qs.getStatus() == QuestStatus.COMPLETE)
					return true;
				QuestTemplate template = DataManager.QUEST_DATA.getQuestById(finishedCond.getQuestId());
				for (XMLStartCondition cond : template.getXMLStartConditions())
					if (hasAnyPreQuestFinished(qsl, cond))
						return true;
			}
		}
		return false;
	}

	/** Start a mission on enter the questZone */
	public boolean defaultOnEnterZoneEvent(QuestEnv env, ZoneName currentZoneName, ZoneName questZoneName) {
		if (questZoneName == currentZoneName) {
			Player player = env.getPlayer();
			if (player == null)
				return false;
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null) {
				env.setQuestId(questId);
				if (QuestService.startQuest(env))
					return true;
			}
		}
		return false;
	}

	public boolean sendQuestRewardDialog(QuestEnv env, int rewardNpcId, int reportDialogId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (env.getTargetId() == rewardNpcId) {
				if (env.getDialogActionId() == USE_OBJECT && reportDialogId != 0) {
					return sendQuestDialog(env, reportDialogId);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	public boolean sendQuestNoneDialog(QuestEnv env, int startNpcId) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		return sendQuestNoneDialog(env, template, startNpcId, 1011);
	}

	public boolean sendQuestNoneDialog(QuestEnv env, int startNpcId, int dialogPageId) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		return sendQuestNoneDialog(env, template, startNpcId, dialogPageId);
	}

	public boolean sendQuestNoneDialog(QuestEnv env, QuestTemplate template, int startNpcId, int dialogPageId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (env.getTargetId() == startNpcId) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, dialogPageId);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}
		return false;
	}

	public boolean sendQuestNoneDialog(QuestEnv env, int startNpcId, int dialogPageId, int itemId, int itemCout) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		return sendQuestNoneDialog(env, template, startNpcId, dialogPageId, itemId, itemCout);
	}

	public boolean sendQuestNoneDialog(QuestEnv env, int startNpcId, int itemId, int itemCout) {
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		return sendQuestNoneDialog(env, template, startNpcId, 1011, itemId, itemCout);
	}

	public boolean sendQuestNoneDialog(QuestEnv env, QuestTemplate template, int startNpcId, int dialogPageId, int itemId, int itemCout) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (env.getTargetId() == startNpcId) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, dialogPageId);
				}
				if (itemId != 0 && itemCout != 0) {
					if (env.getDialogActionId() == QUEST_ACCEPT_1) {
						if (giveQuestItem(env, itemId, itemCout)) {
							return sendQuestStartDialog(env);
						} else {
							return true;
						}
					} else {
						return sendQuestStartDialog(env);
					}
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}
		return false;
	}

	public boolean sendItemCollectingStartDialog(QuestEnv env) {
		switch (env.getDialogActionId()) {
			case QUEST_ACCEPT_1:
				QuestService.startQuest(env);
				return sendQuestSelectionDialog(env);
			case QUEST_REFUSE_1:
				return sendQuestSelectionDialog(env);
		}
		return false;
	}

	public static VisibleObject spawn(int templateId, VisibleObject objectToGetInstanceFrom, float x, float y, float z, byte heading) {
		return spawn(templateId, objectToGetInstanceFrom.getWorldMapInstance(), x, y, z, heading);
	}

	public static VisibleObject spawn(int templateId, WorldMapInstance worldMapInstance, float x, float y, float z, byte heading) {
		SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(worldMapInstance.getMapId(), templateId, x, y, z, heading);
		return SpawnEngine.spawnObject(template, worldMapInstance.getInstanceId());
	}

	public static VisibleObject spawnInFrontOf(int templateId, VisibleObject referencePositionObject) {
		return spawnInFront(templateId, referencePositionObject.getPosition(), null, 1.5f, 0);
	}

	public static VisibleObject spawnForFiveMinutesInFrontOf(int templateId, VisibleObject referencePositionObject, float distance) {
		return spawnInFront(templateId, referencePositionObject.getPosition(), null, distance, 5);
	}

	public static VisibleObject spawnForFiveMinutesInFront(int templateId, VisibleObject referencePositionObject, byte heading, float distance) {
		return spawnInFront(templateId, referencePositionObject.getPosition(), heading, distance, 5);
	}

	private static VisibleObject spawnInFront(int templateId, WorldPosition referencePosition, Byte heading, float distance, int timeInMin) {
		if (heading == null) // make the spawn face towards referencePosition
			heading = (byte) (referencePosition.getHeading() < 60 ? referencePosition.getHeading() + 60 : referencePosition.getHeading() - 60);
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(referencePosition.getHeading()));
		float x = referencePosition.getX() + (float) (Math.cos(radian) * distance);
		float y = referencePosition.getY() + (float) (Math.sin(radian) * distance);
		float z = referencePosition.getZ();
		float geoZ = GeoService.getInstance().getZ(referencePosition.getMapId(), x, y, z + 2, z - 1, referencePosition.getInstanceId());
		if (!Float.isNaN(geoZ))
			z = geoZ;
		return spawnTemporarily(templateId, referencePosition.getWorldMapInstance(), x, y, z, heading, timeInMin);
	}

	public static VisibleObject spawnForFiveMinutes(int templateId, WorldPosition position) {
		return spawnForFiveMinutes(templateId, position, position.getHeading());
	}

	public static VisibleObject spawnForFiveMinutes(int templateId, WorldPosition position, byte heading) {
		return spawnTemporarily(templateId, position.getWorldMapInstance(), position.getX(), position.getY(), position.getZ(), heading, 5);
	}

	public static VisibleObject spawnForFiveMinutes(int templateId, WorldMapInstance worldMapInstance, float x, float y, float z, byte heading) {
		return spawnTemporarily(templateId, worldMapInstance, x, y, z, heading, 5);
	}

	public static VisibleObject spawnTemporarily(int templateId, WorldMapInstance worldMapInstance, float x, float y, float z, byte heading, int timeInMin) {
		VisibleObject object = spawn(templateId, worldMapInstance, x, y, z, heading);
		if (timeInMin > 0)
			ThreadPoolManager.getInstance().schedule(() -> object.getController().deleteIfAliveOrCancelRespawn(), 60000 * timeInMin);
		return object;
	}
}
