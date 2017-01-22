package quest.kaldor;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _13817WeeklyTheWithin extends QuestHandler {

	private static final int questId = 13817;

	public _13817WeeklyTheWithin() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(804585).addOnQuestStart(questId);
		qe.registerQuestNpc(804585).addOnTalkEvent(questId);
		qe.registerQuestNpc(804595).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 804585) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 804595:
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 0, 0, true, false);
						}
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 804585)
				switch (dialog) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
		}
		return false;
	}
}
