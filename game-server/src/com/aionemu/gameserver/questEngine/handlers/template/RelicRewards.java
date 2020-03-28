package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Bobobear, Rolandas, Pad
 */
public class RelicRewards extends AbstractTemplateQuestHandler {

	private final Set<Integer> startNpcIds = new HashSet<>();
	private boolean isDataDriven;

	public RelicRewards(int questId, List<Integer> startNpcIds) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
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
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.contains(targetId)) {
				switch (dialogActionId) {
					case EXCHANGE_COIN:
						QuestTemplate template = DataManager.QUEST_DATA.getQuestById(env.getQuestId());
						if (player.getCommonData().getLevel() >= template.getMinlevelPermitted()) {
							if (QuestService.checkAndGetCollectItemQuestRewardCategory(env) != -1)
								return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
							else
								return sendQuestDialog(env, 3398);
						} else
							return sendQuestDialog(env, 3398);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
			if (startNpcIds.contains(targetId)) {
				int rewardId = -1;
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
					case SELECT1:
						rewardId = QuestService.checkAndGetCollectItemQuestRewardCategory(env, 0);
						break;
					case SELECT2:
						rewardId = QuestService.checkAndGetCollectItemQuestRewardCategory(env, 1);
						break;
					case SELECT3:
						rewardId = QuestService.checkAndGetCollectItemQuestRewardCategory(env, 2);
						break;
					case SELECT4:
						rewardId = QuestService.checkAndGetCollectItemQuestRewardCategory(env, 3);
						break;
				}
				if (rewardId != -1) {
					qs.setRewardGroup(rewardId);
					qs.setQuestVar(rewardId + 1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, rewardId + 5);
				} else
					return sendQuestDialog(env, 1009);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (startNpcIds.contains(targetId)) {
				int var = qs.getQuestVarById(0);
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, var + 4);
					case SELECTED_QUEST_NOREWARD:
						sendQuestEndDialog(env);
						return true;
				}
			}
		}
		return false;
	}
}
