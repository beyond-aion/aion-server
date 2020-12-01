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
public class _2922FascinatingGift extends AbstractQuestHandler {

	public _2922FascinatingGift() {
		super(2922);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204261).addOnQuestStart(questId);
		qe.registerQuestNpc(204261).addOnTalkEvent(questId);
		qe.registerQuestNpc(798058).addOnTalkEvent(questId);
		qe.registerQuestNpc(204108).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204261) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204261) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1003);
				} else if (dialogActionId == SELECT1_1) {
					return sendQuestDialog(env, 1012);
				} else if (dialogActionId == SELECT1_2) {
					return sendQuestDialog(env, 1097);
				} else if (dialogActionId == SETPRO10) {
					qs.setQuestVar(10);
					qs.setRewardGroup(0);
					return defaultCloseDialog(env, 10, 10, true, false);
				} else if (dialogActionId == SETPRO20) {
					qs.setQuestVar(20);
					qs.setRewardGroup(1);
					return defaultCloseDialog(env, 20, 20, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798058 && qs.getQuestVarById(0) == 10) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				}
				return sendQuestEndDialog(env);
			} else if (targetId == 204108 && qs.getQuestVarById(0) == 20) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 1693);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
