package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Bobobear
 */
public class _20065TiamarantanTraining extends QuestHandler {

	private final static int questId = 20065;

	public _20065TiamarantanTraining() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 800070, 800071, 800072, 205864 };
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 800070: {
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1011);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
				}
				case 800071: {
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2);
						}
					}
				}
				case 800072: {
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1693);
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 2, 3, true, false);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 205864: {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
