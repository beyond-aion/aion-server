package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke, Xitanium
 */
public class _1483HarumonerksRequest extends AbstractQuestHandler {

	public _1483HarumonerksRequest() {
		super(1483);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798126).addOnQuestStart(questId);
		qe.registerQuestNpc(798126).addOnTalkEvent(questId);
		qe.registerQuestNpc(203940).addOnTalkEvent(questId);
		qe.registerQuestNpc(203944).addOnTalkEvent(questId);
		qe.registerQuestNpc(798127).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 798126) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203940) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1) {
					changeQuestStep(env, 0, 1);
					giveQuestItem(env, workItems.getFirst().getItemId(), workItems.getFirst().getCount());
					return closeDialogWindow(env);
				}
			}
		} else if (targetId == 203944) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2) {
					changeQuestStep(env, 1, 2);
					giveQuestItem(env, workItems.getLast().getItemId(), workItems.getLast().getCount());
					return closeDialogWindow(env);
				}
			}
		} else if (targetId == 798127) {
			if (qs != null) {
				if (env.getDialogActionId() == QUEST_SELECT && qs.getStatus() == QuestStatus.START)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE)
					changeQuestStep(env, 2, 3, true);
				return sendQuestEndDialog(env);
			}
		}
		return super.onDialogEvent(env);
	}
}
