package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.models.NpcInfos;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

import javolution.util.FastMap;

/**
 * @author Hilgert
 * @modified vlog, Pad
 */
public class ReportToMany extends QuestHandler {

	private final int startItemId;
	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final int startDialogId;
	private final int endDialogId;
	private final int maxVar;
	private final FastMap<List<Integer>, NpcInfos> npcInfos;
	private boolean mission;
	private boolean isDataDriven;
	private QuestItems workItem;

	/**
	 * @param questId
	 * @param startItemId
	 * @param endNpc
	 * @param startDialogId
	 * @param endDialogId
	 * @param maxVar
	 */
	public ReportToMany(int questId, int startItemId, List<Integer> startNpcIds, List<Integer> endNpcIds, FastMap<List<Integer>, NpcInfos> npcInfos,
		int startDialogId, int endDialogId, int maxVar, boolean mission) {
		super(questId);
		this.startItemId = startItemId;
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(this.startNpcIds);
		this.npcInfos = npcInfos;
		this.startDialogId = startDialogId;
		this.endDialogId = endDialogId;
		this.maxVar = maxVar;
		this.mission = mission;
		isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
	}

	@Override
	protected void onWorkItemsLoaded() {
		if (workItems == null)
			return;
		workItem = workItems.get(0);
	}

	@Override
	public void register() {
		if (mission) {
			qe.registerOnLevelChanged(questId);
		}
		if (startItemId != 0)
			qe.registerQuestItem(startItemId, questId);
		else {
			for (Integer startNpcId : startNpcIds) {
				qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
				qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
			}
		}
		for (List<Integer> npcIds : npcInfos.keySet()) {
			for (int npcId : npcIds) {
				qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
			}
		}
		if (!endNpcIds.equals(startNpcIds)) {
			for (Integer endNpcId : endNpcIds)
				qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (startItemId != 0) {
				switch (dialog) {
					case QUEST_ACCEPT:
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						if (QuestService.startQuest(env)) {
							if (workItem != null)
								giveQuestItem(env, workItem.getItemId(), workItem.getCount());
						}
						return closeDialogWindow(env);
					case QUEST_REFUSE:
					case QUEST_REFUSE_1:
					case QUEST_REFUSE_2:
					case QUEST_REFUSE_3:
					case QUEST_REFUSE_4:
					case QUEST_REFUSE_SIMPLE:
						return closeDialogWindow(env);
				}
			}
			if (startNpcIds.isEmpty() || startNpcIds.contains(targetId)) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, startDialogId != 0 ? startDialogId : isDataDriven ? 4762 : 1011);
					case QUEST_ACCEPT:
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						if (workItem != null)
							giveQuestItem(env, workItem.getItemId(), workItem.getCount());
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			NpcInfos targetNpcInfo = null;
			for (NpcInfos npcInfo : npcInfos.values()) {
				if (npcInfo.getNpcIds().contains(targetId)) {
					targetNpcInfo = npcInfo;
					break;
				}
			}
			if (var <= maxVar) {
				if (targetNpcInfo != null) {
					if (dialog == DialogAction.SET_SUCCEED) {
						qs.setQuestVar(maxVar);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}

					int closeDialogId = getCloseDialogId(var, targetNpcInfo);
					if (dialog.id() == closeDialogId) {
						if (dialog != DialogAction.CHECK_USER_HAS_QUEST_ITEM && dialog != DialogAction.CHECK_USER_HAS_QUEST_ITEM_SIMPLE
							|| QuestService.collectItemCheck(env, true)) {
							if (var == maxVar) {
								if (closeDialogId == DialogAction.SELECT_QUEST_REWARD.id() || closeDialogId == DialogAction.CHECK_USER_HAS_QUEST_ITEM_SIMPLE.id()) {
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(env);
									return sendQuestDialog(env, 5);
								}
							} else {
								qs.setQuestVarById(0, var + 1);
							}
							updateQuestStatus(env);
						}
						return sendQuestSelectionDialog(env);
					}

					int dialogId = getDialogId(var, targetNpcInfo);
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, dialogId);
					} else if (dialog.id() == dialogId + 1 && targetNpcInfo.getMovie() != 0) {
						sendQuestDialog(env, dialogId + 1);
						return playQuestMovie(env, targetNpcInfo.getMovie());
					}
				}
			} else if (var > maxVar) {
				if (endNpcIds.contains(targetId)) {
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, endDialogId != 0 ? endDialogId : isDataDriven ? 10002 : 2375);
					} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
						if (startItemId != 0) {
							if (!removeQuestItem(env, startItemId, 1)) {
								return false;
							}
						}
						if (workItem != null) {
							long count = player.getInventory().getItemCountByItemId(workItem.getItemId());
							if (count < workItem.getCount()) {
								return sendQuestSelectionDialog(env);
							}
							removeQuestItem(env, workItem.getItemId(), count, QuestStatus.COMPLETE);
						}
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestEndDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && endNpcIds.contains(targetId)) {
			int var = qs.getQuestVarById(0);
			NpcInfos targetNpcInfo = null;
			for (NpcInfos npcInfo : npcInfos.values()) {
				if (npcInfo.getNpcIds().contains(targetId)) {
					targetNpcInfo = npcInfo;
					break;
				}
			}
			if (var >= maxVar && targetNpcInfo != null) {
				int closeDialogId = getCloseDialogId(var, targetNpcInfo);
				if (dialog == DialogAction.USE_OBJECT) {
					if (closeDialogId == DialogAction.SELECT_QUEST_REWARD.id() || closeDialogId == DialogAction.CHECK_USER_HAS_QUEST_ITEM_SIMPLE.id())
						return sendQuestEndDialog(env);
					int dialogId = getDialogId(var, targetNpcInfo);
					return sendQuestDialog(env, dialogId);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		if (startItemId != 0) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs == null || qs.isStartable()) {
				return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
			}
		}
		return HandlerResult.UNKNOWN;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			constantSpawns.addAll(startNpcIds);
			if (!endNpcIds.equals(startNpcIds)) {
				constantSpawns.addAll(endNpcIds);
			}
			for (List<Integer> npcIds : npcInfos.keySet()) {
				for (int npcId : npcIds) {
					constantSpawns.add(npcId);
				}
			}
		}
		return constantSpawns;
	}

	private int getCloseDialogId(int var, NpcInfos targetNpcInfo) {
		if (targetNpcInfo.getCloseDialog() == 0) {
			if (var == maxVar)
				return 1009;
			else
				return 10000 + var;
		} else {
			return targetNpcInfo.getCloseDialog();
		}
	}

	private int getDialogId(int var, NpcInfos targetNpcInfo) {
		if (targetNpcInfo.getQuestDialog() == 0) {
			if (var == maxVar)
				return isDataDriven ? 10002 : 2375;
			else
				return (isDataDriven ? 1011 : 1352) + var * 341;
		} else {
			return targetNpcInfo.getQuestDialog();
		}
	}
}
