package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke, Xitanium
 */
public class _1484ChiyorinrinerksRequest extends AbstractQuestHandler {

	public _1484ChiyorinrinerksRequest() {
		super(1484);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798127).addOnQuestStart(questId);
		qe.registerQuestNpc(798127).addOnTalkEvent(questId);
		qe.registerQuestNpc(204045).addOnTalkEvent(questId);
		qe.registerQuestNpc(204048).addOnTalkEvent(questId);
		qe.registerQuestNpc(204011).addOnTalkEvent(questId);
		qe.registerQuestNpc(798126).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 798127) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 204045) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1) {
					changeQuestStep(env, 0, 1);
					giveQuestItem(env, workItems.getFirst().getItemId(), workItems.getFirst().getCount());
					return closeDialogWindow(env);
				}
			}
		} else if (targetId == 204048) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2) {
					changeQuestStep(env, 1, 2);
					giveQuestItem(env, workItems.get(1).getItemId(), workItems.get(1).getCount());
					return closeDialogWindow(env);
				}
			}
		} else if (targetId == 204011) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (env.getDialogActionId() == SETPRO3) {
					changeQuestStep(env, 2, 3);
					giveQuestItem(env, workItems.getLast().getItemId(), workItems.getLast().getCount());
					return closeDialogWindow(env);
				}
			}
		} else if (targetId == 798126) {
			if (qs != null) {
				if (env.getDialogActionId() == QUEST_SELECT && qs.getStatus() == QuestStatus.START)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE)
					changeQuestStep(env, 3, 4, true);
				return sendQuestEndDialog(env);
			}
		}
		return super.onDialogEvent(env);
	}
}
