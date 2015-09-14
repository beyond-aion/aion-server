package quest.raksang;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _18709ReachingOuttothePast extends QuestHandler {

	private static final int questId = 18709;

	public _18709ReachingOuttothePast() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799429).addOnQuestStart(questId);
		qe.registerQuestNpc(799429).addOnTalkEvent(questId);
		qe.registerQuestNpc(203890).addOnTalkEvent(questId);
		qe.registerQuestNpc(203864).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 799429) {
				switch (env.getDialog()) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					case QUEST_ACCEPT_SIMPLE: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 203890) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SELECT_ACTION_1353:
						return sendQuestDialog(env, 1353);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 203864) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 1, 1, true);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203864) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
