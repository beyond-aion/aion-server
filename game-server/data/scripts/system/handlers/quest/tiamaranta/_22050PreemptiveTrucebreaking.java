package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author nrg
 */
public class _22050PreemptiveTrucebreaking extends QuestHandler {

	private static final int questId = 22050, start_npc = 205865;

	public _22050PreemptiveTrucebreaking() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(start_npc).addOnQuestStart(questId);
		qe.registerQuestNpc(start_npc).addOnTalkEvent(questId);
		qe.registerOnKillRanked(AbyssRankEnum.GRADE5_SOLDIER, questId);
	}

	@Override
	public boolean onKillRankedEvent(QuestEnv env) {
		return defaultOnKillRankedEvent(env, 0, 10, true); // reward
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getTargetId() == start_npc) {
			if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
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
