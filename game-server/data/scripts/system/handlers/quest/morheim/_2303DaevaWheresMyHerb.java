package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author vlog
 */
public class _2303DaevaWheresMyHerb extends QuestHandler {

	private static final int questId = 2303;
	private int choice = 0;

	public _2303DaevaWheresMyHerb() {
		super(questId);
	}

	@Override
	public void register() {
		int[] mobs = { 211298, 211305, 211304, 211297 };
		qe.registerQuestNpc(798082).addOnQuestStart(questId); // Bicorunerk
		qe.registerQuestNpc(798082).addOnTalkEvent(questId); // Bicorunerk
		qe.registerQuestNpc(204378).addOnTalkEvent(questId); // Favyr
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

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798082) { // Bicorunerk
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case ASK_QUEST_ACCEPT:
						return sendQuestDialog(env, 4);
					case QUEST_ACCEPT_1:
						return sendQuestDialog(env, 1003);
					case QUEST_REFUSE_1:
						return sendQuestDialog(env, 1004);
					case SETPRO10:
						if (QuestService.startQuest(env)) {
							changeQuestStep(env, 0, 11); // 11
							choice = 0;
							return sendQuestDialog(env, 1012);
						} else {
							return sendQuestSelectionDialog(env);
						}
					case SETPRO20:
						if (QuestService.startQuest(env)) {
							changeQuestStep(env, 0, 21); // 21
							choice = 1;
							return sendQuestDialog(env, 1097);
						} else {
							return sendQuestSelectionDialog(env);
						}
					case FINISH_DIALOG:
						return sendQuestSelectionDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 798082) { // Bicorunerk
				if (dialog == DialogAction.FINISH_DIALOG) {
					return sendQuestSelectionDialog(env);
				} else if (dialog == DialogAction.USE_OBJECT) {
					if (var == 0) {
						return sendQuestDialog(env, 1003);
					} else {
						return sendQuestSelectionDialog(env);
					}
				} else if (dialog == DialogAction.SETPRO10) {
					changeQuestStep(env, 0, 11); // 11
					choice = 0;
					return sendQuestDialog(env, 1012);
				} else if (dialog == DialogAction.SETPRO20) {
					changeQuestStep(env, 0, 21); // 21
					choice = 1;
					return sendQuestDialog(env, 1097);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204378) { // Favyr
				switch (dialog) {
					case USE_OBJECT:
						if (var == 15) {
							return sendQuestDialog(env, 1353);
						} else if (var == 25) {
							return sendQuestDialog(env, 1438);
						}
						return false;
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5 + choice);
					default: {
						return sendQuestEndDialog(env, choice);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			int[] daru = { 211298, 211305 };
			int[] ettins = { 211304, 211297 };
			if (var >= 11 && var < 15) {
				return defaultOnKillEvent(env, daru, 10, 15); // 15
			} else if (var == 15) {
				switch (targetId) {
					case 211298:
					case 211305:
						qs.setQuestVar(15);
						qs.setStatus(QuestStatus.REWARD); // reward
						updateQuestStatus(env);
						return true;
				}
			} else if (var >= 21 && var < 25) {
				return defaultOnKillEvent(env, ettins, 20, 25); // 25
			} else if (var == 25) {
				switch (targetId) {
					case 211304:
					case 211297:
						qs.setQuestVar(25);
						qs.setStatus(QuestStatus.REWARD); // reward
						updateQuestStatus(env);
						return true;
				}
			}
		}
		return false;
	}
}
