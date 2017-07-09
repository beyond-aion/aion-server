package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _11227EasyAs extends AbstractQuestHandler {

	public _11227EasyAs() {
		super(11227);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799076).addOnQuestStart(questId);
		qe.registerQuestNpc(799076).addOnTalkEvent(questId);
		qe.registerQuestNpc(217071).addOnKillEvent(questId);
		qe.registerQuestNpc(217070).addOnKillEvent(questId);
		qe.registerQuestNpc(217069).addOnKillEvent(questId);
		qe.registerQuestNpc(217068).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799076) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799076) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 217071, 0, false) || defaultOnKillEvent(env, 217070, 1, false) || defaultOnKillEvent(env, 217069, 2, false)
			|| defaultOnKillEvent(env, 217068, 3, true);
	}
}
