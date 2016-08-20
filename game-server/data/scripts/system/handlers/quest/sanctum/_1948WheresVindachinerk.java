package quest.sanctum;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _1948WheresVindachinerk extends QuestHandler {

	private static final int questId = 1948;

	public _1948WheresVindachinerk() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 798012, 798004, 798132, 279006 };
		qe.registerQuestNpc(798012).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798012) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 798004: {
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						}
						case SELECT_ACTION_1353: {
							playQuestMovie(env, 0);
							return sendQuestDialog(env, 1353);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
					return false;
				}
				case 798132: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1693);
							break;
						case SELECT_ACTION_1694:
							return sendQuestDialog(env, 1694);
						case SETPRO2:
							changeQuestStep(env, 1, 2, false);
							return defaultCloseDialog(env, 2, 3, true, false);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 279006) {
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
