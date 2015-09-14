package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Evil_dnk
 */
public class _23010AFreshPosting extends QuestHandler {

	private final static int questId = 23010;

	public _23010AFreshPosting() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801108).addOnQuestStart(questId);
		qe.registerQuestNpc(801108).addOnTalkEvent(questId);
		qe.registerQuestNpc(801107).addOnTalkEvent(questId);
		qe.registerQuestNpc(801109).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801108) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					case QUEST_ACCEPT_SIMPLE: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 801107) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 0) {
							return sendQuestDialog(env, 1352);
						}

					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}

				}
			} else if (targetId == 801109) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 1) {
							return sendQuestDialog(env, 2375);
						}
					}
					case SELECT_QUEST_REWARD: {
						changeQuestStep(env, 1, 2, true); // reward
						return sendQuestDialog(env, 5);

					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801109) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
