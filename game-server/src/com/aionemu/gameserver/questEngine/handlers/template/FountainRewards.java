package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Wakizashi, vlog, Bobobear, Luzien, Pad
 */
public class FountainRewards extends AbstractTemplateQuestHandler {

	private final Set<Integer> startNpcIds = new HashSet<>();

	public FountainRewards(int questId, List<Integer> startNpcIds) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
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
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		
		if (qs == null || qs.isStartable()) {
			if (startNpcIds.contains(targetId)) { // Coin Fountain
				switch (dialogActionId) {
					case USE_OBJECT:
						if (!QuestService.inventoryItemCheck(env, true)) {
							return true;
						} else
							return sendQuestSelectionDialog(env);
					case SETPRO1:
						if (QuestService.collectItemCheck(env, false)) {
							if (!player.getInventory().isFullSpecialCube()) {
								if (QuestService.startQuest(env)) {
									changeQuestStep(env, 0, 0, true);
									return sendQuestDialog(env, 5);
								}
							} else {
								PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY());
								return sendQuestSelectionDialog(env);
							}
						} else {
							return sendQuestSelectionDialog(env);
						}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (startNpcIds.contains(targetId)) { // Coin Fountain
				if (dialogActionId == SELECTED_QUEST_NOREWARD) {
					if (QuestService.collectItemCheck(env, true))
						return sendQuestEndDialog(env);
				} else {
					return QuestService.abandonQuest(player, questId);
				}
			}
		}
		return false;
	}
}
