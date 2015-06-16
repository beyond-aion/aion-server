package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.Iterator;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.quest.CollectItem;
import com.aionemu.gameserver.model.templates.quest.CollectItems;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.quest.QuestWorkItems;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.models.WorkOrdersData;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.RecipeService;
import com.aionemu.gameserver.services.item.ItemService;

/**
 * @author Mr. Poke reworked Bobobear
 */
public class WorkOrders extends QuestHandler {

	private final WorkOrdersData workOrdersData;

	/**
	 * @param questId
	 */
	public WorkOrders(WorkOrdersData workOrdersData) {
		super(workOrdersData.getId());
		this.workOrdersData = workOrdersData;
	}

	@Override
	public void register() {
		Iterator<Integer> iterator = workOrdersData.getStartNpcIds().iterator();
		while (iterator.hasNext()) {
			int startNpc = iterator.next();
			qe.registerQuestNpc(startNpc).addOnQuestStart(workOrdersData.getId());
			qe.registerQuestNpc(startNpc).addOnTalkEvent(workOrdersData.getId());
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		if (workOrdersData.getStartNpcIds().contains(targetId)) {
			QuestState qs = player.getQuestStateList().getQuestState(workOrdersData.getId());
			if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
				switch (env.getDialog()) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, DialogPage.ASK_QUEST_ACCEPT_WINDOW.id());
					}
					case QUEST_ACCEPT_1: {
						if (RecipeService.validateNewRecipe(player, workOrdersData.getRecipeId()) != null) {
							if (QuestService.startQuest(env)) {
								if (ItemService.addQuestItems(player, workOrdersData.getGiveComponent())) {
									RecipeService.addRecipe(player, workOrdersData.getRecipeId(), false);
									closeDialogWindow(env);
								}
								return true;
							}
						}
					}
					case CRAFT: {
						env.setQuestId(0);
						return sendQuestDialog(env, DialogPage.COMBINETASK_WINDOW.id());
					}
				}
			}
			else if (qs.getStatus() == QuestStatus.START) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					int var = qs.getQuestVarById(0);
					if (QuestService.collectItemCheck(env, false)) {
						changeQuestStep(env, var, var, true); // reward
						QuestWorkItems qwi = DataManager.QUEST_DATA.getQuestById(workOrdersData.getId()).getQuestWorkItems();
						if (qwi != null) {
							long count = 0;
							for (QuestItems qi : qwi.getQuestWorkItem()) {
								if (qi != null) {
									count = player.getInventory().getItemCountByItemId(qi.getItemId());
									if (count > 0)
										player.getInventory().decreaseByItemId(qi.getItemId(), count);
								}
							}
						}
						return sendQuestDialog(env, DialogPage.SELECT_QUEST_REWARD_WINDOW1.id());
					}
					else {
						return sendQuestSelectionDialog(env);
					}
				}
			}
			else if (qs.getStatus() == QuestStatus.REWARD) {
				QuestTemplate template = DataManager.QUEST_DATA.getQuestById(workOrdersData.getId());
				CollectItems collectItems = template.getCollectItems();
				long count = 0;
				for (CollectItem collectItem : collectItems.getCollectItem()) {
					count = player.getInventory().getItemCountByItemId(collectItem.getItemId());
					if (count > 0)
						player.getInventory().decreaseByItemId(collectItem.getItemId(), count);
				}
				player.getRecipeList().deleteRecipe(player, workOrdersData.getRecipeId());
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					QuestService.finishQuest(env, 0);
					env.setQuestId(workOrdersData.getId());
					return sendQuestDialog(env, 1008);
				}
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			constantSpawns.addAll(workOrdersData.getStartNpcIds());
		}
		return constantSpawns;
	}
}
