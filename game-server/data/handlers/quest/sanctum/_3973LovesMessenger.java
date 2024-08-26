package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _3973LovesMessenger extends AbstractQuestHandler {

	private final static int questId = 3973;

	public _3973LovesMessenger() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(203893).addOnQuestStart(questId);
		qe.registerQuestNpc(203893).addOnTalkEvent(questId);
		qe.registerQuestNpc(203792).addOnTalkEvent(questId);
		qe.registerQuestNpc(203793).addOnTalkEvent(questId);
		qe.registerQuestNpc(798391).addOnTalkEvent(questId);
		qe.registerQuestNpc(798949).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		if (qs == null || qs.isStartable()) {
			if (targetId == 203893) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203792) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1352);
					}
				} else if (env.getDialogActionId() == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			}
			if (targetId == 203793) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1) {
						return sendQuestDialog(env, 1693);
					}
				} else if (env.getDialogActionId() == SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			}
			if (targetId == 798391) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2) {
						return sendQuestDialog(env, 2034);
					}
				} else if (env.getDialogActionId() == SETPRO3) {
					return defaultCloseDialog(env, 2, 3);
				}
			}
			if (targetId == 798949) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 3) {
						return sendQuestDialog(env, 2375);
					}
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
