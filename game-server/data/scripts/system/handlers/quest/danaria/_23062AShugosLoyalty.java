package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _23062AShugosLoyalty extends QuestHandler {

	private static final int questId = 23062;
	private static int[] mobs = { 230557, 230558 };

	public _23062AShugosLoyalty() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801133).addOnQuestStart(questId);
		qe.registerQuestNpc(801144).addOnTalkEvent(questId);
		qe.registerQuestNpc(801140).addOnTalkEvent(questId);
		qe.registerQuestNpc(801133).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 801133) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 801144) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
					case SETPRO1:
						changeQuestStep(env, 0, 1, false);
						return closeDialogWindow(env);
				}
			} else if (targetId == 801140) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
					case SETPRO2:
						changeQuestStep(env, 1, 2, false);
						return closeDialogWindow(env);
				}
			} else if (targetId == 801133) {
				switch (dialog) {
					case QUEST_SELECT:
						/*
						 * if(var == 11) return sendQuestDialog(env, 2375);
						 */
						if (var == 9)
							return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						// changeQuestStep(env, 11, 11, true);
						changeQuestStep(env, 9, 9, true);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801133)
				return sendQuestEndDialog(env);
		}

		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		// return (defaultOnKillEvent(env, mobs, 2, 11));
		return (defaultOnKillEvent(env, mobs, 0, 9));
	}

}
