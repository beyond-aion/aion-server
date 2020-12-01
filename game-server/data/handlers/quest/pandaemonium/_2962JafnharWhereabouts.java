package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _2962JafnharWhereabouts extends AbstractQuestHandler {

	public _2962JafnharWhereabouts() {
		super(2962);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204253).addOnQuestStart(questId);
		qe.registerQuestNpc(204253).addOnTalkEvent(questId);
		qe.registerQuestNpc(278067).addOnTalkEvent(questId);
		qe.registerQuestNpc(278137).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204253) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 278067) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1011);
					}
				} else if (dialogActionId == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 278137) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 204253) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2)
						return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SELECT3_1) {
					return sendQuestDialog(env, 1694);
				} else if (dialogActionId == SELECT3_2) {
					return sendQuestDialog(env, 1779);
				} else if (dialogActionId == SETPRO3) {
					qs.setRewardGroup(0);
					return defaultCloseDialog(env, 2, 2, true, true);
				} else if (dialogActionId == SETPRO4) {
					qs.setRewardGroup(1);
					return defaultCloseDialog(env, 2, 2, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204253)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
