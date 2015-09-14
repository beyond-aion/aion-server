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
public class _11524FieldRepairs extends QuestHandler {

	private final static int questId = 11524;

	public _11524FieldRepairs() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205533).addOnQuestStart(questId);
		qe.registerQuestNpc(205533).addOnTalkEvent(questId);
		qe.registerQuestNpc(205562).addOnTalkEvent(questId);
		qe.registerQuestNpc(205536).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 205533) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205536) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 205562) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1693);
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 205533) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 2, 2, true);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205533)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
