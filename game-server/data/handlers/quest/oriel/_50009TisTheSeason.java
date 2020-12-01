package quest.oriel;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Bobobear
 */
public class _50009TisTheSeason extends AbstractQuestHandler {

	public _50009TisTheSeason() {
		super(50009);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831032).addOnQuestStart(questId);
		qe.registerQuestNpc(831038).addOnQuestStart(questId);
		qe.registerQuestNpc(831032).addOnTalkEvent(questId);
		qe.registerQuestNpc(831038).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 831032 || targetId == 831038) {
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
			if (targetId == 831032 || targetId == 831038) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 0, 0, true);
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831032 || targetId == 831038)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
