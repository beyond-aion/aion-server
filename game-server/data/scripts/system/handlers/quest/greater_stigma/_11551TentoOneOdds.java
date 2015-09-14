package quest.greater_stigma;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _11551TentoOneOdds extends QuestHandler {

	private final static int questId = 11551;

	public _11551TentoOneOdds() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205531).addOnQuestStart(questId);
		qe.registerQuestNpc(205531).addOnTalkEvent(questId);
		qe.registerOnKillInWorld(300350000, questId);
		qe.registerOnKillInWorld(300360000, questId);
	}

	@Override
	public boolean onKillInWorldEvent(QuestEnv env) {
		return defaultOnKillRankedEvent(env, 0, 10, true); // reward
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (env.getTargetId() == 205531) {
			if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
