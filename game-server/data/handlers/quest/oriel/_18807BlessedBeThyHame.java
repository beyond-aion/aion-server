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
public class _18807BlessedBeThyHame extends AbstractQuestHandler {

	public _18807BlessedBeThyHame() {
		super(18807);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830194).addOnQuestStart(questId);
		qe.registerQuestNpc(830194).addOnTalkEvent(questId);
		qe.registerQuestNpc(730524).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 830194) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 730524:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SELECT2_1:
							return sendQuestDialog(env, 1353);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					break;
				case 830194:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case SELECT_QUEST_REWARD:
							changeQuestStep(env, 1, 1, true);
							return sendQuestDialog(env, 5);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 830194) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
