package quest.morheim;

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
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Nephis
 */
public class _2324TheSpiritsNotebook extends AbstractQuestHandler {

	public _2324TheSpiritsNotebook() {
		super(2324);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204373).addOnTalkEvent(questId);
		qe.registerQuestItem(182204123, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null)
			return false;

		if (targetId == 0) {
			if (env.getDialogActionId() == QUEST_ACCEPT_1) {
				qs.setStatus(QuestStatus.START);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
				return true;
			}
		} else if (targetId == 204373) {
			if (qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE) {
					removeQuestItem(env, 182204123, 1);
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

		if (id != 182204123)
			return HandlerResult.UNKNOWN;
		sendQuestDialog(env, 4);
		return HandlerResult.SUCCESS;
	}
}
