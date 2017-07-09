package quest.pernon;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Bobobear
 */
public class _51009TheGiftOfCharity extends AbstractQuestHandler {

	public _51009TheGiftOfCharity() {
		super(51009);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831033).addOnQuestStart(questId);
		qe.registerQuestNpc(831039).addOnQuestStart(questId);
		qe.registerQuestNpc(831033).addOnTalkEvent(questId);
		qe.registerQuestNpc(831039).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 831033 || targetId == 831039) {
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
			if (targetId == 831033 || targetId == 831039) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 0, 0, true);
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831033 || targetId == 831039)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
