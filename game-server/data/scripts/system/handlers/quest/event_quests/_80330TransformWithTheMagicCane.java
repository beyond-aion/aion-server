package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Bobobear
 */
public class _80330TransformWithTheMagicCane extends AbstractQuestHandler {

	public _80330TransformWithTheMagicCane() {
		super(80330);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831527).addOnQuestStart(questId);
		qe.registerQuestNpc(831527).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 831527) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 831527)
				switch (dialogActionId) {
					case QUEST_SELECT:
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 0, 0, true); // reward
						return sendQuestDialog(env, 5);
				}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831527)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
