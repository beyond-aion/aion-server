package quest.greater_stigma;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 * @reworked vlog
 */
public class _21277OutoftheirColdDeadHands extends QuestHandler {

	private final static int questId = 21277;

	public _21277OutoftheirColdDeadHands() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799208).addOnQuestStart(questId);
		qe.registerQuestNpc(799208).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 799208) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799208: {
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 0, 0, true, 5, 2716); // reward
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799208) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
