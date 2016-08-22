package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _2448ChaomirkSendsForHelp extends QuestHandler {

	private final static int questId = 2448;

	int rewardIndex;

	public _2448ChaomirkSendsForHelp() {
		super(questId);
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
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798115) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182204210, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798115) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1003);
				} else if (dialog == DialogAction.SELECT_ACTION_1012) {
					return sendQuestDialog(env, 1012);
				} else if (dialog == DialogAction.SELECT_ACTION_1097) {
					return sendQuestDialog(env, 1097);
				} else if (dialog == DialogAction.SETPRO10) {
					changeQuestStep(env, 0, 10);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				} else if (dialog == DialogAction.SETPRO20) {
					rewardIndex = 1;
					changeQuestStep(env, 0, 20);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798080 && qs.getQuestVarById(0) == 10) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				}
			} else if (targetId == 798079 && qs.getQuestVarById(0) == 20) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 1693);
				}
			}
			removeQuestItem(env, 182204210, 1);
			return sendQuestEndDialog(env, rewardIndex);
		}
		return false;
	}
}
