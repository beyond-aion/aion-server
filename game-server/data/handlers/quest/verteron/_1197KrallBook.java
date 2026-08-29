package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Nephis, Rolandas
 */
public class _1197KrallBook extends AbstractQuestHandler {

	public _1197KrallBook() {
		super(1197);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(700004).addOnTalkEvent(questId);
		qe.registerQuestNpc(203129).addOnTalkEvent(questId);
		qe.registerQuestItem(182200558, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (!(env.getVisibleObject() instanceof Npc npc)) {
			if (env.getDialogActionId() == QUEST_ACCEPT_1) {
				QuestService.startQuest(env);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
				return true;
			}
		} else if (npc.getNpcId() == 700004) {
			if (qs == null || qs.isStartable()) {
				if (player.getInventory().getItemCountByItemId(182200558) == 0 && giveQuestItem(env, 182200558, 1)) {
					npc.getController().deleteAndScheduleRespawn();
				}
			}
			return true;
		} else if (npc.getNpcId() == 203129) {
			if (qs != null) {
				if (env.getDialogActionId() == QUEST_SELECT && qs.getStatus() == QuestStatus.START) {
					return sendQuestDialog(env, 2375);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE) {
					removeQuestItem(env, 182200558, 1);
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
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		int id = item.getItemTemplate().getTemplateId();

		if (id != 182200558)
			return HandlerResult.UNKNOWN;
		sendQuestDialog(env, 4);
		return HandlerResult.SUCCESS;
	}
}
