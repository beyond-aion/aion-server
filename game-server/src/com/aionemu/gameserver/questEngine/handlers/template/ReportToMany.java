package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

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

/**
 * @author Hilgert
 * @modified vlog, Pad
 * @reworked Neon
 */
public class ReportToMany extends QuestHandler {

	private final int startItemId;
	private final Set<Integer> startNpcIds = new HashSet<>();
	private final int startDialogId;
	private final List<NpcInfos> npcInfos = new ArrayList<>();
	private final boolean mission;
	private final boolean isDataDriven;

	public ReportToMany(int questId, int startItemId, List<Integer> startNpcIds, List<NpcInfos> npcInfos, int startDialogId, boolean mission) {
		super(questId);
		this.startItemId = startItemId;
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		this.npcInfos.addAll(npcInfos);
		this.startDialogId = startDialogId;
		this.mission = mission;
		this.isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
		if (workItems != null && workItems.size() > this.npcInfos.size())
			LoggerFactory.getLogger(ReportToMany.class).warn("Q{} has more work items than quest steps", questId);
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
		for (NpcInfos npcInfo : npcInfos) {
			for (int npcId : npcInfo.getNpcIds()) {
				qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
			}
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if ((startNpcIds.isEmpty() || startNpcIds.contains(targetId))
				&& (startItemId == 0 || player.getInventory().getFirstItemByItemId(startItemId) != null)) {
				switch (dialog) {
					case QUEST_ACCEPT:
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						QuestService.startQuest(env);
						return closeDialogWindow(env);
					case QUEST_SELECT:
						return sendQuestDialog(env, startDialogId != 0 ? startDialogId : isDataDriven ? 4762 : 1011);
					default:
						return super.onDialogEvent(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int step = qs.getQuestVarById(0); // starting from 0
			if (step > getMaxStep()) {
				LoggerFactory.getLogger(ReportToMany.class).warn("Missing NpcInfo for quest " + questId + " step #" + (step + 1));
				return false;
			}
			NpcInfos targetNpcInfo = npcInfos.get(step);
			if (!targetNpcInfo.getNpcIds().contains(targetId))
				return false;

			switch (dialog) {
				case QUEST_SELECT:
					return sendQuestDialog(env, getDialogId(step));
				case SETPRO1:
				case SETPRO2:
				case SETPRO3:
				case SETPRO4:
				case SETPRO5:
				case SETPRO6:
				case SETPRO7:
				case SETPRO8:
				case SETPRO9:
				case SETPRO10:
				case SETPRO11:
				case SETPRO12:
					changeQuestStep(env, step, step + 1);
					if (workItems != null && workItems.size() > step)
						giveQuestItem(env, workItems.get(step).getItemId(), workItems.get(step).getCount());
					return closeDialogWindow(env);
				case SET_SUCCEED:
				case SELECT_QUEST_REWARD:
				case CHECK_USER_HAS_QUEST_ITEM:
				case CHECK_USER_HAS_QUEST_ITEM_SIMPLE:
					if (dialog == DialogAction.SET_SUCCEED) // set reward from pre-end npc (end npc is another one who will then give the reward)
						step++;
					if (step < getMaxStep() || !validateAndRemoveItems(env, dialog))
						return sendQuestSelectionDialog(env);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				default:
					if (targetNpcInfo.getMovie() != 0)
						playQuestMovie(env, targetNpcInfo.getMovie());
					return super.onDialogEvent(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			NpcInfos endNpcInfo = npcInfos.get(getMaxStep());
			if (!endNpcInfo.getNpcIds().contains(targetId))
				return false;
			int step = qs.getQuestVarById(0);
			boolean rewardStatusFromRewardNpc = step == getMaxStep(); // false if reward status was set by SET_SUCCEED action
			if (dialog == DialogAction.USE_OBJECT && !rewardStatusFromRewardNpc)
				return sendQuestDialog(env, isDataDriven ? 10002 : 2375); // show full reward dialog instead of last page
			return sendQuestEndDialog(env);
		}
		return false;
	}

	private int getMaxStep() {
		return npcInfos.size() - 1;
	}

	private int getDialogId(int var) {
		if (var == getMaxStep())
			return isDataDriven ? 10002 : 2375;
		else
			return (isDataDriven ? 1011 : 1352) + var * 341;
	}

	private boolean validateAndRemoveItems(QuestEnv env, DialogAction dialog) {
		if (!QuestService.collectItemCheck(env, true))
			return false;
		if (startItemId != 0 && !removeQuestItem(env, startItemId, 1))
			return false;
		if (workItems != null) {
			for (QuestItems workItem : workItems)
				removeQuestItem(env, workItem.getItemId(), workItem.getCount(), QuestStatus.COMPLETE);
		}
		return true;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		if (startItemId != 0) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs.isStartable()) {
				return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
			}
		}
		return HandlerResult.UNKNOWN;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
