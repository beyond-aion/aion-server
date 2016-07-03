package quest.event_quests;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _80009TheCakeIsTheTruth extends QuestHandler {

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
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						removeQuestItem(env, 182214007, 1);
						return defaultCloseDialog(env, 0, 1, true, true);
				}
			}
		}
		return sendQuestRewardDialog(env, 798417, 0);
	}
}
