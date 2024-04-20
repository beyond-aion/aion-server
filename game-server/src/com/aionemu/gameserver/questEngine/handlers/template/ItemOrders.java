package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Altaress, Bobobear, Pad
 */
public class ItemOrders extends AbstractTemplateQuestHandler {

	private static final Logger log = LoggerFactory.getLogger(ItemOrders.class);

	private int startItemId;
	private final int talkNpcId1;
	private final int talkNpcId2;
	private final int endNpcId;

	public ItemOrders(int questId, int talkNpcId1, int talkNpcId2, int endNpcId) {
		super(questId);
		this.talkNpcId1 = talkNpcId1;
		this.talkNpcId2 = talkNpcId2;
		this.endNpcId = endNpcId;
		if (workItems == null) {
			log.warn("Q{} has no work item", questId);
		} else {
			if (workItems.size() > 1)
				log.warn("Q{} has more than 1 work item", questId);
			this.startItemId = workItems.get(0).getItemId();
		}
	}

	@Override
	public void register() {
		qe.registerQuestItem(startItemId, questId);
		if (talkNpcId1 != 0)
			qe.registerQuestNpc(talkNpcId1).addOnTalkEvent(questId);
		if (talkNpcId2 != 0)
			qe.registerQuestNpc(talkNpcId2).addOnTalkEvent(questId);
		if (endNpcId != 0)
			qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			switch (dialogActionId) {
				case QUEST_ACCEPT:
				case QUEST_ACCEPT_1:
				case QUEST_ACCEPT_SIMPLE:
					if (player.getInventory().getItemCountByItemId(startItemId) > 0) {
						QuestService.startQuest(env);
					} else {
						String requiredItemL10n = DataManager.ITEM_DATA.getItemTemplate(startItemId).getL10n();
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_INVENTORY_ITEM(requiredItemL10n));
					}
					return closeDialogWindow(env);
				default:
					return super.onDialogEvent(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var0 = qs.getQuestVarById(0);
			if (targetId == talkNpcId1 || targetId == talkNpcId2) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO1) {
					boolean reward = ((var0 == 0 && talkNpcId2 == 0) || (var0 == 1 && talkNpcId2 != 0));
					qs.setQuestVarById(0, var0 + 1);
					if (reward)
						qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				}
			} else if (targetId == endNpcId) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 0, 1, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == endNpcId) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2375);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
