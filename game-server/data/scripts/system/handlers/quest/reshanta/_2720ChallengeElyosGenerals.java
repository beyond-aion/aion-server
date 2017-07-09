package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * Fight and defeat Elyos Generals (3). Report back to Votan (278001).
 * 
 * @author vlog
 */
public class _2720ChallengeElyosGenerals extends AbstractQuestHandler {

	private final static int _questId = 2720;
	private final static AbyssRankEnum _general = AbyssRankEnum.GENERAL;

	public _2720ChallengeElyosGenerals() {
		super(_questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278001).addOnTalkEvent(_questId);
		qe.registerQuestNpc(278001).addOnQuestStart(_questId);
		qe.registerOnKillRanked(_general, _questId);
	}

	@Override
	public boolean onKillRankedEvent(QuestEnv env) {
		return defaultOnKillRankedEvent(env, 0, 3, true); // reward
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(_questId);

		if (env.getTargetId() == 278001) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
