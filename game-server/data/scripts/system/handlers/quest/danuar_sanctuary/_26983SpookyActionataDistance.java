package quest.danuar_sanctuary;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _26983SpookyActionataDistance extends QuestHandler {

	private static final int questId = 26983;

	public _26983SpookyActionataDistance() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801954).addOnQuestStart(questId);
		qe.registerQuestNpc(801547).addOnTalkEvent(questId);
		qe.registerQuestNpc(701862).addOnTalkEvent(questId);
		qe.registerQuestNpc(701863).addOnTalkEvent(questId);
		qe.registerQuestNpc(701864).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 801954) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 701862:
				case 701863:
				case 701864:
					useQuestObject(env, 0, 1, true, false);
					return closeDialogWindow(env);
			}

		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801547) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}

		return false;
	}

}
