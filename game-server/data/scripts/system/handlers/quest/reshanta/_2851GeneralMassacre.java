package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author Luzien
 */
public class _2851GeneralMassacre extends AbstractQuestHandler {

	public _2851GeneralMassacre() {
		super(2851);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278001).addOnQuestStart(questId);
		qe.registerQuestNpc(278001).addOnTalkEvent(questId);
		qe.registerOnKillRanked(AbyssRankEnum.GENERAL, questId);
	}

	@Override
	public boolean onKillRankedEvent(QuestEnv env) {
		return defaultOnKillRankedEvent(env, 0, 3, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getTargetId() == 278001) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				if (env.getTargetId() == 278001) {
					if (env.getDialogActionId() == USE_OBJECT) {
						return sendQuestDialog(env, 1352);
					} else {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
}
