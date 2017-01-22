package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _1169LightningfootTuka extends QuestHandler {

	private final static int questId = 1169;

	public _1169LightningfootTuka() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203126).addOnQuestStart(questId);
		qe.registerQuestNpc(203126).addOnTalkEvent(questId);
		qe.registerQuestNpc(210317).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 210317, 0, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (env.getTargetId() == 203126) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (env.getTargetId() == 203126) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
