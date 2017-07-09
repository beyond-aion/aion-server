package quest.oriel;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _18847Housewarming extends AbstractQuestHandler {

	public _18847Housewarming() {
		super(18847);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830226).addOnQuestStart(questId);
		qe.registerQuestNpc(830226).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 830226) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
					case QUEST_REFUSE_1:
					case QUEST_REFUSE_SIMPLE:
						return sendQuestDialog(env, 1004);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 830226) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1003);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 0, 0, true);
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 830226) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
