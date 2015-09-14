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
public class _22500NapTimeIsOver extends QuestHandler {

	private static final int questId = 22500;

	public _22500NapTimeIsOver() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(800529).addOnQuestStart(questId);
		qe.registerQuestNpc(800529).addOnTalkEvent(questId);
		qe.registerQuestNpc(801001).addOnTalkEvent(questId);
		qe.registerQuestNpc(801007).addOnTalkEvent(questId);
		qe.registerQuestNpc(801255).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 800529) {
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
			if (targetId == 801001) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1352);
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			} else if (targetId == 801007) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1693);
					}
					case SETPRO2: {
						return defaultCloseDialog(env, 1, 2);
					}
				}
			} else if (targetId == 801255) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 2034);
					}
					case SETPRO3: {
						qs.setQuestVar(3);
						return defaultCloseDialog(env, 3, 3, true, false);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800529) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
