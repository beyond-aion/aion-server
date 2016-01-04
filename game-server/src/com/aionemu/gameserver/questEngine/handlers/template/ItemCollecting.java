package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestBonuses;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.reward.BonusService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 * @reworked vlog, Rolandas
 * @Modified Majka (2015.07.13) - Added management of the new properties checkOkDialogId, checkFailDialogId
 */
public class ItemCollecting extends QuestHandler {

	private static final Logger log = LoggerFactory.getLogger(ItemCollecting.class);

	private final Set<Integer> startNpcs = new HashSet<Integer>();
	private final Set<Integer> endNpcs = new HashSet<Integer>();
	private final int questMovie;
	private final int nextNpcId;
	private final int startDialogId;
	private final int startDialogId2;
	private final int checkOkDialogId;
	private final int checkFailDialogId;
	private QuestItems workItem;

	public ItemCollecting(int questId, List<Integer> startNpcIds, int nextNpcId, List<Integer> endNpcIds, int questMovie, int startDialogId,
		int startDialogId2, int checkOkDialogId, int checkFailDialogId) {
		super(questId);
		startNpcs.addAll(startNpcIds);
		startNpcs.remove(0);
		this.nextNpcId = nextNpcId;
		if (endNpcIds == null) {
			endNpcs.addAll(startNpcs);
		} else {
			endNpcs.addAll(endNpcIds);
			endNpcs.remove(0);
		}
		this.questMovie = questMovie;
		this.startDialogId = startDialogId;
		this.startDialogId2 = startDialogId2;
		this.checkOkDialogId = checkOkDialogId;
		this.checkFailDialogId = checkFailDialogId;
	}

	@Override
	protected void onWorkItemsLoaded() {
		if (workItems == null)
			return;
		if (workItems.size() > 1) {
			log.warn("Q{} (ItemCollecting) has more than 1 work item.", questId);
		}
		workItem = workItems.get(0);
	}

	@Override
	public void register() {
		Iterator<Integer> iterator = startNpcs.iterator();
		while (iterator.hasNext()) {
			int startNpc = iterator.next();
			qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
			qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
		}
		if (nextNpcId != 0) {
			qe.registerQuestNpc(nextNpcId).addOnTalkEvent(getQuestId());
		}
		if (actionItems != null) {
			iterator = actionItems.iterator();
			while (iterator.hasNext()) {
				int actionItem = iterator.next();
				qe.registerQuestNpc(actionItem).addOnTalkEvent(getQuestId());
				qe.registerCanAct(getQuestId(), actionItem);
			}
		}

		iterator = endNpcs.iterator();
		while (iterator.hasNext()) {
			int endNpc = iterator.next();
			qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(getQuestId());
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (startNpcs.isEmpty() || startNpcs.contains(targetId) || DataManager.QUEST_DATA.getQuestById(questId).getCategory() == QuestCategory.FACTION) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, startDialogId != 0 ? startDialogId : 1011);
					}
					case SETPRO1: {
						QuestService.startQuest(env);
						return closeDialogWindow(env);
					}
					case SELECT_ACTION_1012: {
						if (questMovie != 0) {
							playQuestMovie(env, questMovie);
						}
						return sendQuestDialog(env, 1012);
					}
					case QUEST_ACCEPT:
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						if (workItem != null) {
							// Some quest work items come from other quests, don't add again
							long currentCount = workItem.getCount();
							currentCount -= player.getInventory().getItemCountByItemId(workItem.getItemId());
							if (currentCount > 0)
								giveQuestItem(env, workItem.getItemId(), currentCount, ItemAddType.QUEST_WORK_ITEM);
						}
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == nextNpcId && var == 0) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1352);
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			} else if (endNpcs.contains(targetId)) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, startDialogId2 != 0 ? startDialogId2 : 2375);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						int okDialogId = checkOkDialogId != 0 ? checkOkDialogId : 5;
						int failDialogId = checkFailDialogId != 0 ? checkFailDialogId : 2716;
						return checkQuestItems(env, var, var, true, okDialogId, failDialogId); // reward
					}
					case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: {
						return checkQuestItemsSimple(env, var, var, true, 5, 0, 0); // reward
					}
					case FINISH_DIALOG: {
						return sendQuestSelectionDialog(env);
					}
					case SET_SUCCEED: {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
					case SETPRO1: {
						return checkQuestItemsSimple(env, var, var, true, 5, 0, 0);
					}
					case SETPRO2: {
						return checkQuestItemsSimple(env, var, var, true, 6, 0, 0);
					}
					case SETPRO3: {
						return checkQuestItemsSimple(env, var, var, true, 7, 0, 0);
					}
					case SETPRO4: {
						return checkQuestItemsSimple(env, var, var, true, 8, 0, 0);
					}
				}
			}

			else if (targetId != 0 && actionItems != null && actionItems.contains(targetId)) {
				return true; // looting
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(env.getQuestId());
			List<QuestBonuses> bonuses = template.getBonus();
			if (!bonuses.isEmpty() && bonuses.get(0).getType() == BonusType.MEDAL && !BonusService.getInstance().checkInventory(player, template)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DECOMPRESS_INVENTORY_IS_FULL);
				return closeDialogWindow(env);
			}
			if (endNpcs.contains(targetId)) {
				if (workItem != null) {
					long currentCount = player.getInventory().getItemCountByItemId(workItem.getItemId());
					if (currentCount > 0)
						removeQuestItem(env, workItem.getItemId(), currentCount, QuestStatus.COMPLETE);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			if (startNpcs != null)
				constantSpawns.addAll(startNpcs);
			if (endNpcs != null)
				constantSpawns.addAll(endNpcs);
			if (actionItems != null)
				constantSpawns.addAll(actionItems);
		}
		return constantSpawns;
	}

}
