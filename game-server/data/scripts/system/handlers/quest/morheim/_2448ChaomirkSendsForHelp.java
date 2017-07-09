package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _2448ChaomirkSendsForHelp extends AbstractQuestHandler {

	public _2448ChaomirkSendsForHelp() {
		super(2448);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798115).addOnQuestStart(questId);
		qe.registerQuestNpc(798080).addOnTalkEvent(questId);
		qe.registerQuestNpc(798079).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798115) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182204210, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798115) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1003);
				} else if (dialogActionId == SELECT1_1) {
					return sendQuestDialog(env, 1012);
				} else if (dialogActionId == SELECT1_2) {
					return sendQuestDialog(env, 1097);
				} else if (dialogActionId == SETPRO10) {
					changeQuestStep(env, 0, 10);
					qs.setRewardGroup(0);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				} else if (dialogActionId == SETPRO20) {
					changeQuestStep(env, 0, 20);
					qs.setRewardGroup(1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798080 && qs.getQuestVarById(0) == 10) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				}
			} else if (targetId == 798079 && qs.getQuestVarById(0) == 20) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 1693);
				}
			}
			removeQuestItem(env, 182204210, 1);
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
