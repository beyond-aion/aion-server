package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.CollectItem;
import com.aionemu.gameserver.model.templates.quest.CollectItems;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.RecipeService;
import com.aionemu.gameserver.services.item.ItemService;

/**
 * @author Mr. Poke, Bobobear, Pad
 */
public class WorkOrders extends AbstractTemplateQuestHandler {

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final List<QuestItems> giveComponents = new ArrayList<>();
	private final int recipeId;

	public WorkOrders(int questId, List<Integer> startNpcIds, List<QuestItems> giveComponents, int recipeId) {
		super(questId);
		this.startNpcIds.addAll(startNpcIds);
		this.giveComponents.addAll(giveComponents);
		this.recipeId = recipeId;
	}

	@Override
	public void register() {
		for (Integer startNpcId : startNpcIds) {
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (startNpcIds.contains(targetId)) {
			if (qs == null || qs.isStartable()) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, DialogPage.ASK_QUEST_ACCEPT_WINDOW.id());
					case QUEST_ACCEPT_1:
						if (RecipeService.validateNewRecipe(player, recipeId) != null) {
							if (QuestService.startQuest(env)) {
								for (QuestItems qi : giveComponents)
									ItemService.addItem(player, qi.getItemId(), qi.getCount(), true);
								RecipeService.addRecipe(player, recipeId, false);
								closeDialogWindow(env);
								return true;
							}
						}
						return false;
					case COMBINE_TASK:
						env.setQuestId(0);
						return sendQuestDialog(env, DialogPage.COMBINETASK_WINDOW.id());
				}
			} else if (qs.getStatus() == QuestStatus.START) {
				if (dialogActionId == QUEST_SELECT) {
					int var = qs.getQuestVarById(0);
					if (QuestService.collectItemCheck(env, false)) {
						changeQuestStep(env, var, var, true); // reward
						QuestService.removeQuestWorkItems(player, qs);
						return sendQuestDialog(env, DialogPage.SELECT_QUEST_REWARD_WINDOW1.id());
					} else {
						return sendQuestSelectionDialog(env);
					}
				}
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				CollectItems collectItems = DataManager.QUEST_DATA.getQuestById(questId).getCollectItems();
				long count = 0;
				for (CollectItem collectItem : collectItems.getCollectItem()) {
					count = player.getInventory().getItemCountByItemId(collectItem.getItemId());
					if (count > 0)
						player.getInventory().decreaseByItemId(collectItem.getItemId(), count);
				}
				player.getRecipeList().deleteRecipe(player, recipeId);
				if (dialogActionId == USE_OBJECT) {
					QuestService.finishQuest(env);
					env.setQuestId(questId);
					return sendQuestDialog(env, 1008);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
