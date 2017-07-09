package quest.shugo_imperial_tomb;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _80275EventEmpiresPast extends AbstractQuestHandler {

	public _80275EventEmpiresPast() {
		super(80275);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831117).addOnQuestStart(questId);
		qe.registerQuestNpc(831117).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 831117) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 831117:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							if (var == 0)
								return sendQuestDialog(env, 2375);
							return false;
						}
						case SELECT_QUEST_REWARD: {
							changeQuestStep(env, 0, 0, true);
							return sendQuestDialog(env, 5);
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831117)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
