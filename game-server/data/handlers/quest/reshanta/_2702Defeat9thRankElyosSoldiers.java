package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author Hilgert, vlog
 */
public class _2702Defeat9thRankElyosSoldiers extends AbstractQuestHandler {

	public _2702Defeat9thRankElyosSoldiers() {
		super(2702);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278016).addOnQuestStart(questId);
		qe.registerQuestNpc(278016).addOnTalkEvent(questId);
		qe.registerOnKillRanked(AbyssRankEnum.GRADE9_SOLDIER, questId);
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
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				if (env.getTargetId() == 278016) {
					if (env.getDialogActionId() == USE_OBJECT)
						return sendQuestDialog(env, 1352);
					else
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
