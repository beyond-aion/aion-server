package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke
 * @modified Rolandas, Pad
 */
public class ReportTo extends QuestHandler {

	private static final Logger log = LoggerFactory.getLogger(ReportTo.class);

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final int startDialogId;
	private final int endDialogId;
	private final boolean isDataDriven;
	private QuestItems workItem;

	/**
	 * @param id
	 * @param startNpcIds
	 * @param endNpcIds
	 * @param itemId2
	 */
	public ReportTo(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, int startDialogId, int endDialogId) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(this.startNpcIds);
		this.startDialogId = startDialogId;
		this.endDialogId = endDialogId;
		isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
	}

	@Override
	protected void onWorkItemsLoaded() {
		if (workItems == null)
			return;
		if (workItems.size() > 1) {
			log.warn("Q{} (ReportTo) has more than 1 work item.", questId);
		}
		workItem = workItems.get(0);
	}

	@Override
	public void register() {
		for (Integer startNpcId : startNpcIds) {
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}
		if (!endNpcIds.equals(startNpcIds)) {
			for (Integer endNpcId : endNpcIds) {
				qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
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
			if (endNpcIds.contains(targetId)) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, endDialogId != 0 ? endDialogId : isDataDriven ? 10002 : 2375);
					}
					case SELECT_QUEST_REWARD: {
						if (workItem != null) {
							long currentCount = player.getInventory().getItemCountByItemId(workItem.getItemId());
							if (currentCount < workItem.getCount()) {
								return sendQuestSelectionDialog(env);
							}
							removeQuestItem(env, workItem.getItemId(), currentCount, QuestStatus.COMPLETE);
						}
						qs.setQuestVar(1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestEndDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcIds.contains(targetId)) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			constantSpawns.addAll(startNpcIds);
			if (!endNpcIds.equals(startNpcIds))
				constantSpawns.addAll(endNpcIds);
		}
		return constantSpawns;
	}
}
