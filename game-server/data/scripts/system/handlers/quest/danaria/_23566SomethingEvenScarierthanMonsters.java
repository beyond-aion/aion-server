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
public class _23566SomethingEvenScarierthanMonsters extends QuestHandler {

	private static final int questId = 23566;
	private static int[] mobs = { 230563, 230564 };

	public _23566SomethingEvenScarierthanMonsters() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801146).addOnQuestStart(questId);
		qe.registerQuestNpc(801146).addOnTalkEvent(questId);
		qe.registerQuestNpc(230563).addOnTalkEvent(questId);
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
			if (targetId == 801146) {
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
			if (targetId == 230563) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
					case SETPRO1:
						changeQuestStep(env, 0, 1, false);
						return closeDialogWindow(env);

				}
			} else if (targetId == 801146) {
				switch (dialog) {
					case QUEST_SELECT:
						/*
						 * if(var == 11) return sendQuestDialog(env, 2375);
						 */
						if (var == 10)
							return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 10, 11, true);
						// changeQuestStep(env, 11, 11, true);
						return sendQuestDialog(env, 5);

				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801146)
				return sendQuestEndDialog(env);
		}

		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		// return (defaultOnKillEvent(env, mobs, 1, 11));
		return (defaultOnKillEvent(env, mobs, 0, 10));
	}

}
