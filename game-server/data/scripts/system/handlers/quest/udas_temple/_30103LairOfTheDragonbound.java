package quest.udas_temple;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _30103LairOfTheDragonbound extends QuestHandler {

	private static final int questId = 30103;

	public _30103LairOfTheDragonbound() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799333).addOnQuestStart(questId);
		qe.registerQuestNpc(799333).addOnTalkEvent(questId);
		qe.registerGetingItem(182209189, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799333) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 799333) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (var == 1) {
						return sendQuestDialog(env, 2375);
					}
				} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					changeQuestStep(env, 1, 1, true); // reward
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799333) {
				removeQuestItem(env, 182209189, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 0, 1, false); // 1
	}
}
