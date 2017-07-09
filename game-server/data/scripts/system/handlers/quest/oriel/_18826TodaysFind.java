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
public class _18826TodaysFind extends AbstractQuestHandler {

	public _18826TodaysFind() {
		super(18826);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830520).addOnQuestStart(questId);
		qe.registerQuestNpc(830520).addOnTalkEvent(questId);
		qe.registerQuestNpc(830660).addOnQuestStart(questId);
		qe.registerQuestNpc(830660).addOnTalkEvent(questId);
		qe.registerQuestNpc(830661).addOnQuestStart(questId);
		qe.registerQuestNpc(830661).addOnTalkEvent(questId);
		qe.registerQuestNpc(730522).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 830520 || targetId == 830660 || targetId == 830661) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 730522) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 0, 0, true);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && targetId == 730522) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
