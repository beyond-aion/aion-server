package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _11511SurprisingSolutions extends QuestHandler {

	private final static int questId = 11511;

	public _11511SurprisingSolutions() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205989).addOnQuestStart(questId);
		qe.registerQuestNpc(205989).addOnTalkEvent(questId);
		qe.registerQuestNpc(205746).addOnTalkEvent(questId);
		qe.registerGetingItem(182213115, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 205989) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					default: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205746) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1352);
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			} else if (targetId == 205989) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 2375);
					}
					case CHECK_USER_HAS_QUEST_ITEM_SIMPLE: {
						return checkQuestItemsSimple(env, 1, 1, true, 5, 0, 0); // reward
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205989)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		Player player = env.getPlayer();
		if (player == null)
			return false;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(1) == 15) {
				changeQuestStep(env, 1, 1, false);
				return true;
			} else {
				qs.setQuestVarById(1, qs.getQuestVarById(1));
			}
		}
		return false;
	}
}
