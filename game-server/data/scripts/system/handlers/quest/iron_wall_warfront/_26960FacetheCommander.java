package quest.iron_wall_warfront;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr.Madison
 */
public class _26960FacetheCommander extends AbstractQuestHandler {

	public _26960FacetheCommander() {
		super(26960);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801280).addOnQuestStart(questId);
		qe.registerQuestNpc(801280).addOnTalkEvent(questId);
		qe.registerQuestNpc(802055).addOnTalkEvent(questId);
		qe.registerQuestNpc(802054).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 801280) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 802054) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SELECT2_1:
						return sendQuestDialog(env, 1353);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801280) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 233544, 0, true);
	}

}
