package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _1948WheresVindachinerk extends AbstractQuestHandler {

	public _1948WheresVindachinerk() {
		super(1948);
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
		int dialogActionId = env.getDialogActionId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 798012) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 798004:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						}
						case SELECT2_1: {
							playQuestMovie(env, 0);
							return sendQuestDialog(env, 1353);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
					return false;
				case 279006:
					if (var == 2) {
						switch (dialogActionId) {
							case QUEST_SELECT:
								return sendQuestDialog(env, 2375);
							case SELECT_QUEST_REWARD:
								changeQuestStep(env, var, var, true);
								return sendQuestEndDialog(env);
						}
					}
					return false;
				case 798132:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1693);
							break;
						case SELECT3_1:
							return sendQuestDialog(env, 1694);
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 279006)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
