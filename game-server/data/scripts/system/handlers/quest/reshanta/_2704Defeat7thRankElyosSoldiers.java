package quest.reshanta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author Hilgert
 * @modified vlog
 */
public class _2704Defeat7thRankElyosSoldiers extends QuestHandler {

	private final static int questId = 2704;

	public _2704Defeat7thRankElyosSoldiers() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278016).addOnQuestStart(questId);
		qe.registerQuestNpc(278016).addOnTalkEvent(questId);
		qe.registerOnKillRanked(AbyssRankEnum.GRADE7_SOLDIER, questId);
	}

	@Override
	public boolean onKillRankedEvent(QuestEnv env) {
		return defaultOnKillRankedEvent(env, 0, 10, true); // reward
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getTargetId() == 278016) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialog() == DialogAction.USE_OBJECT || env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
