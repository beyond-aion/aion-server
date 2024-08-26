package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author MrPoke, Nephis (http://www.evolution-fr.com/)
 */
public class _2316VivisBook extends AbstractQuestHandler {

	public _2316VivisBook() {
		super(2316);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204386).addOnTalkEvent(questId);
		qe.registerQuestItem(182204115, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (targetId == 0) {
			if (env.getDialogActionId() == ASK_QUEST_ACCEPT) {
				return sendQuestDialog(env, 4);
			} else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
				return sendQuestStartDialog(env);
			} else if (env.getDialogActionId() == QUEST_REFUSE_1) {
				return closeDialogWindow(env);
			}
		} else if (targetId == 204386) {
			if (qs != null) {
				if (env.getDialogActionId() == QUEST_SELECT && qs.getStatus() == QuestStatus.START) {
					return sendQuestDialog(env, 2375);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE) {
					removeQuestItem(env, 182204115, 1);
					qs.setQuestVar(1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				} else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if (id != 182204115)
			return HandlerResult.UNKNOWN;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
			}
		}, 20);
		return HandlerResult.SUCCESS;
	}
}
