package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Npc;
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
 */
public class RelicRewards extends QuestHandler {

	private final Set<Integer> startNpcs = new HashSet<Integer>();

	/**
	 * @param questId
	 * @param startNpcId
	 */
	public RelicRewards(int questId, List<Integer> startNpcIds) {
		super(questId);
		this.startNpcs.addAll(startNpcIds);
		this.startNpcs.remove(0);
	}

	@Override
	public void register() {
		Iterator<Integer> iterator = startNpcs.iterator();
		while (iterator.hasNext()) {
			int startNpc = iterator.next();
			qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
			qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (startNpcs.contains(targetId)) {
				switch (env.getDialog()) {
					case EXCHANGE_COIN: {
						if (player.getCommonData().getLevel() >= 30) {
							int rewardId = QuestService.getCollectItemsReward(env, false, false);
							if (rewardId != -1)
								return sendQuestDialog(env, 1011);
							else
								return sendQuestDialog(env, 3398);
						}
						else
							return sendQuestDialog(env, 3398);
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
			if (startNpcs.contains(targetId)) {
				int rewardId = -1;
				switch (env.getDialog()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 1011);
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
					qs.setCompleteCount(0);
					updateQuestStatus(env);
					return sendQuestDialog(env, rewardId + 5);
				}
				else
					return sendQuestDialog(env, 1009);
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (startNpcs.contains(targetId)) {
				int var = qs.getQuestVarById(0);
				switch (env.getDialog()) {
					case USE_OBJECT:
						if (var == 1)
							return sendQuestDialog(env, 5);
						else if (var == 2)
							return sendQuestDialog(env, 6);
						else if (var == 3)
							return sendQuestDialog(env, 7);
						else if (var == 4)
							return sendQuestDialog(env, 8);
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
		HashSet<Integer> constantSpawns = new HashSet<>();
		if (startNpcs != null)
			constantSpawns.addAll(startNpcs);
		return constantSpawns;
	}
}
