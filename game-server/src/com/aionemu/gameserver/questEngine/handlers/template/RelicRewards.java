package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Bobobear, Rolandas
 * @modified Pad
 */
public class RelicRewards extends QuestHandler {

	private final Set<Integer> startNpcIds = new HashSet<>();
	private boolean isDataDriven;

	/**
	 * @param questId
	 * @param startNpcId
	 */
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
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.contains(targetId)) {
				switch (dialog) {
					case EXCHANGE_COIN:
						if (player.getCommonData().getLevel() >= 30) {
							int rewardId = QuestService.getCollectItemsReward(env, false, false);
							if (rewardId != -1)
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
				switch (dialog) {
					case USE_OBJECT:
						return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
					case SELECT_ACTION_1011:
						rewardId = QuestService.checkCollectItemsReward(env, false, true, 0);
						break;
					case SELECT_ACTION_1352:
						rewardId = QuestService.checkCollectItemsReward(env, false, true, 1);
						break;
					case SELECT_ACTION_1693:
						rewardId = QuestService.checkCollectItemsReward(env, false, true, 2);
						break;
					case SELECT_ACTION_2034:
						rewardId = QuestService.checkCollectItemsReward(env, false, true, 3);
						break;
				}
				if (rewardId != -1) {
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
				switch (dialog) {
					case USE_OBJECT:
						return sendQuestDialog(env, var + 4);
					case SELECTED_QUEST_NOREWARD:
						QuestService.finishQuest(env, qs.getQuestVars().getQuestVars() - 1);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			constantSpawns.addAll(startNpcIds);
		}
		return constantSpawns;
	}
}
