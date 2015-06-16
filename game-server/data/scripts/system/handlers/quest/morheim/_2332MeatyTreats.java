package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author stpavel
 */

public class _2332MeatyTreats extends QuestHandler {

	private final static int questId = 2332;

	public _2332MeatyTreats() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798084).addOnQuestStart(questId);
		qe.registerQuestNpc(798084).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798084) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 798084) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					if (QuestService.collectItemCheck(env, true))
						return sendQuestDialog(env, 1352);
					else
						return sendQuestDialog(env, 1693);
				}
				else if (env.getDialogId() >= 10000 && env.getDialogId() <= 10002) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + (env.getDialogId() - 10000));
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, env.getDialogId() - 9995);

				}
			}
		}
		else if (env.getDialogId() == DialogAction.SELECTED_QUEST_NOREWARD.id() && qs.getStatus() == QuestStatus.REWARD && targetId == 798084) {
			QuestService.finishQuest(env, qs.getQuestVarById(0));
			return sendQuestDialog(env, 1008);
		}
		return false;
	}
}
