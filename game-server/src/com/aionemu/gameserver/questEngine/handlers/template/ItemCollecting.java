package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke, vlog, Rolandas, Majka, Pad
 */
public class ItemCollecting extends AbstractTemplateQuestHandler {

	private static final Logger log = LoggerFactory.getLogger(ItemCollecting.class);

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final int questMovie;
	private final int nextNpcId;
	private final int startDialogId;
	private final int startDialogId2;
	private final int checkOkDialogId;
	private final int checkFailDialogId;
	private final boolean isDataDriven;
	private final String startZone;
	private QuestItems workItem;

	public ItemCollecting(int questId, List<Integer> startNpcIds, int nextNpcId, List<Integer> endNpcIds, String startZone, int questMovie,
		int startDialogId, int startDialogId2, int checkOkDialogId, int checkFailDialogId) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		this.nextNpcId = nextNpcId;
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(this.startNpcIds);
		this.startZone = startZone;
		this.questMovie = questMovie;
		this.startDialogId = startDialogId;
		this.startDialogId2 = startDialogId2;
		this.checkOkDialogId = checkOkDialogId;
		this.checkFailDialogId = checkFailDialogId;
		this.isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
		if (workItems != null) {
			if (workItems.size() > 1)
				log.warn("Q{} has more than 1 work item", questId);
			workItem = workItems.get(0);
		}
	}

	@Override
	public void register() {
		for (Integer startNpcId : startNpcIds) {
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}
		if (nextNpcId != 0) {
			qe.registerQuestNpc(nextNpcId).addOnTalkEvent(questId);
		}
		if (!endNpcIds.equals(startNpcIds)) {
			for (Integer endNpcId : endNpcIds)
				qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
		}
		if (actionItems != null) {
			for (Integer actionItem : actionItems) {
				qe.registerQuestNpc(actionItem).addOnTalkEvent(questId);
				qe.registerCanAct(questId, actionItem);
			}
		}
		if (startZone != null && !ZoneName.get(startZone).name().equalsIgnoreCase("NONE"))
			qe.registerOnEnterZone(ZoneName.get(startZone), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.isEmpty() || startNpcIds.contains(targetId)
				|| DataManager.QUEST_DATA.getQuestById(questId).getCategory() == QuestCategory.FACTION) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, startDialogId != 0 ? startDialogId : isDataDriven ? 4762 : 1011);
					case SETPRO1:
						QuestService.startQuest(env);
						return closeDialogWindow(env);
					case SELECT1_1:
						if (questMovie != 0) {
							playQuestMovie(env, questMovie);
						}
						return sendQuestDialog(env, 1012);
					case QUEST_ACCEPT:
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env, workItem);
					default:
						return super.onDialogEvent(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == nextNpcId && var == 0) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (endNpcIds.contains(targetId)) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, startDialogId2 != 0 ? startDialogId2 : isDataDriven ? 1011 : 2375);
					case CHECK_USER_HAS_QUEST_ITEM:
						int okDialogId = checkOkDialogId != 0 ? checkOkDialogId : isDataDriven ? 10000 : 5;
						int failDialogId = checkFailDialogId != 0 ? checkFailDialogId : isDataDriven ? 10001 : 2716;
						return checkQuestItems(env, var, var, true, okDialogId, failDialogId); // reward
					case CHECK_USER_HAS_QUEST_ITEM_SIMPLE:
						return checkQuestItemsSimple(env, var, var, true, 5, 0, 0); // reward
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);
					case SET_SUCCEED:
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					case SETPRO1:
						return checkQuestItemsSimple(env, var, var, true, 5, 0, 0);
					case SETPRO2:
						return checkQuestItemsSimple(env, var, var, true, 6, 0, 0);
					case SETPRO3:
						return checkQuestItemsSimple(env, var, var, true, 7, 0, 0);
					case SETPRO4:
						return checkQuestItemsSimple(env, var, var, true, 8, 0, 0);
				}
			} else if (actionItems != null && actionItems.contains(targetId)) {
				return true; // looting
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcIds.contains(targetId)) {
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
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName.name().equalsIgnoreCase(startZone)) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs.isStartable()) {
				QuestService.startQuest(env);
				return true;
			}
		}
		return false;
	}
}
