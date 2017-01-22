package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _11228HeNeverReturned extends QuestHandler {

	private final static int questId = 11228;

	public _11228HeNeverReturned() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799077).addOnQuestStart(questId);
		qe.registerQuestNpc(799077).addOnTalkEvent(questId);
		qe.registerQuestNpc(798979).addOnTalkEvent(questId);
		qe.registerQuestNpc(257225).addOnTalkEvent(questId);
		qe.registerQuestNpc(799078).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799077) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798979) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				} else if (dialog == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 257225) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1693);
				} else if (dialog == DialogAction.SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 799078) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2)
						return sendQuestDialog(env, 2034);
				} else if (dialog == DialogAction.SETPRO3) {
					qs.setQuestVar(3);
					return defaultCloseDialog(env, 3, 3, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799077) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
