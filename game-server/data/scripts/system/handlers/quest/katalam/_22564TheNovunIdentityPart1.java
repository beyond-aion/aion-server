package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _22564TheNovunIdentityPart1 extends QuestHandler {

	private final static int questId = 22564;

	public _22564TheNovunIdentityPart1() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801004).addOnQuestStart(questId);
		qe.registerQuestNpc(801004).addOnTalkEvent(questId);
		qe.registerQuestNpc(730802).addOnTalkEvent(questId);
		qe.registerQuestNpc(206317).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801004) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 730802) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialog == DialogAction.SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 801004) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 2, 3, true, 10000, 10001);
				} else if (dialog == DialogAction.SET_SUCCEED) {
					return sendQuestDialog(env, 10002);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801004) {
				switch (dialog) {
					case SET_SUCCEED:
					case USE_OBJECT: {
						return sendQuestDialog(env, 5);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 0)
				changeQuestStep(env, 0, 1, false);
			return true;

		}
		return false;
	}
}
