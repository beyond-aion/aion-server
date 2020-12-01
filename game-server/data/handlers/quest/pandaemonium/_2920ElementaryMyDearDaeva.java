package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _2920ElementaryMyDearDaeva extends AbstractQuestHandler {

	public _2920ElementaryMyDearDaeva() {
		super(2920);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204141).addOnQuestStart(questId);
		qe.registerQuestNpc(204141).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204141) { // Deyla
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204141) { // Deyla
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SETPRO1:
						return sendQuestDialog(env, 1352);
					case SETPRO2:
						return sendQuestDialog(env, 1693);
					case SETPRO11:
						changeQuestStep(env, 0, 0, true); // reward
						qs.setRewardGroup(0);
						return sendQuestDialog(env, 5);
					case SETPRO12:
						changeQuestStep(env, 0, 0, true); // reward
						qs.setRewardGroup(1);
						return sendQuestDialog(env, 6);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204141) { // Deyla
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
