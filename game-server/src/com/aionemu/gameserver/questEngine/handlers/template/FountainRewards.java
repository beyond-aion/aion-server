package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Wakizashi, vlog, Bobobear
 * @reworked Luzien
 * @modified Pad
 */
public class FountainRewards extends QuestHandler {

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
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (startNpcIds.contains(targetId)) { // Coin Fountain
				switch (dialog) {
					case USE_OBJECT: {
						if (!QuestService.inventoryItemCheck(env, true)) {
							return true;
						} else
							return sendQuestSelectionDialog(env);
					}
					case SETPRO1: {
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
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (startNpcIds.contains(targetId)) { // Coin Fountain
				if (dialog == DialogAction.SELECTED_QUEST_NOREWARD) {
					if (QuestService.collectItemCheck(env, true))
						return sendQuestEndDialog(env);
				} else {
					return QuestService.abandonQuest(player, questId);
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
