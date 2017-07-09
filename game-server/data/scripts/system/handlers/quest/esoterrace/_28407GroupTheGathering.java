package quest.esoterrace;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _28407GroupTheGathering extends AbstractQuestHandler {

	public _28407GroupTheGathering() {
		super(28407);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799557).addOnQuestStart(questId);
		qe.registerQuestNpc(799557).addOnTalkEvent(questId);
		qe.registerQuestNpc(730380).addOnTalkEvent(questId);
		qe.registerQuestNpc(799558).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (targetId == 799557) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2)
					return defaultCloseDialog(env, 1, 2);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 730380) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1)
					return defaultCloseDialog(env, 0, 1, 182215016, 1, 0, 0);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 799558) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				} else
					return sendQuestStartDialog(env);
			} else if (qs != null && qs.getStatus() == QuestStatus.REWARD)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
