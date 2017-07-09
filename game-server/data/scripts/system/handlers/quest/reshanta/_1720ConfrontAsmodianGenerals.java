package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * [Group] Confront Asmodian Generals Defeat Asmodian Generals and win (3). -> Asmodian Army General Report the result to Michalis (278501).
 * minlevel_permitted="40" cannot_share="true" race_permitted="ELYOS"
 * 
 * @author vlog
 */
public class _1720ConfrontAsmodianGenerals extends AbstractQuestHandler {

	private final static int _questId = 1720;
	private final static AbyssRankEnum _general = AbyssRankEnum.GENERAL;

	public _1720ConfrontAsmodianGenerals() {
		super(_questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278501).addOnTalkEvent(_questId);
		qe.registerQuestNpc(278501).addOnQuestStart(_questId);
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

		if (env.getTargetId() == 278501) {
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
