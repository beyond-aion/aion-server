package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _80009TheCakeIsTheTruth extends AbstractQuestHandler {

	public _80009TheCakeIsTheTruth() {
		super(80009);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798417).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();

		if (env.getTargetId() == 0)
			return sendQuestStartDialog(env);

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 798417) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 2375);
						return false;
					case SELECT_QUEST_REWARD:
						removeQuestItem(env, 182214007, 1);
						return defaultCloseDialog(env, 0, 1, true, true);
				}
			}
		}
		return sendQuestRewardDialog(env, 798417, 0);
	}
}
