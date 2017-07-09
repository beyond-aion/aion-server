package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Akiro
 */
public class _3913ASecretSummons extends AbstractQuestHandler {

	public _3913ASecretSummons() {
		super(3913);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203725).addOnQuestStart(questId);
		qe.registerQuestNpc(203725).addOnTalkEvent(questId);
		qe.registerQuestNpc(203752).addOnTalkEvent(questId);
		qe.registerQuestNpc(204656).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203725) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case ASK_QUEST_ACCEPT:
						return sendQuestDialog(env, 4);
					case QUEST_ACCEPT_1:
						return sendQuestStartDialog(env);
				}
				return super.onDialogEvent(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203752) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SELECT2_1:
						return sendQuestDialog(env, 1353);
					case SELECT2_1_1:
						return sendQuestDialog(env, 1354);
					case SELECT2_1_1_1:
						return sendQuestDialog(env, 1355);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 204656) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						return defaultCloseDialog(env, 1, 2, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204656) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
