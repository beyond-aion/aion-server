package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Altaress, Bobobear
 */
public class ItemOrders extends QuestHandler {

	private static final Logger log = LoggerFactory.getLogger(ItemOrders.class);

	private int startItemId;
	private final int talkNpc1;
	private final int talkNpc2;
	private final int endNpcId;

	public ItemOrders(int questId, int talkNpc1, int talkNpc2, int endNpcId) {
		super(questId);
		this.talkNpc1 = talkNpc1;
		this.talkNpc2 = talkNpc2;
		this.endNpcId = endNpcId;
	}

	@Override
	protected void onWorkItemsLoaded() {
		if (workItems == null) {
			log.warn("Q{} is not ItemOrders quest.", questId);
			return;
		}
		if (workItems.size() > 1) {
			log.warn("Q{} has more than 1 work item.", questId);
		}
		this.startItemId = workItems.get(0).getItemId();
	}

	@Override
	public void register() {
		qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
		qe.registerQuestItem(startItemId, questId);
		if (talkNpc1 != 0) {
			qe.registerQuestNpc(talkNpc1).addOnTalkEvent(questId);
		}
		if (talkNpc2 != 0) {
			qe.registerQuestNpc(talkNpc2).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 0) {
				if (dialog == DialogAction.QUEST_ACCEPT_1) {
					if (player.getInventory().getItemCountByItemId(startItemId) > 0) {
						QuestService.startQuest(env);
					} else {
						int requiredItemNameId = DataManager.ITEM_DATA.getItemTemplate(startItemId).getNameId();
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_INVENTORY_ITEM(new DescriptionId(requiredItemNameId)));
					}
					return closeDialogWindow(env);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if ((targetId == talkNpc1 && talkNpc1 != 0) || (targetId == talkNpc2 && talkNpc2 != 0)) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialog == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1, true, false);
				}
			} else if ((talkNpc1 == 0) && (talkNpc2 == 0) && targetId == endNpcId) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 0, 1, true, true);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == endNpcId) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 2375);
					}
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
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			if (talkNpc1 != 0)
				constantSpawns.add(talkNpc1);
			if (talkNpc2 != 0)
				constantSpawns.add(talkNpc2);
			constantSpawns.add(endNpcId);
		}
		return constantSpawns;
	}

}
