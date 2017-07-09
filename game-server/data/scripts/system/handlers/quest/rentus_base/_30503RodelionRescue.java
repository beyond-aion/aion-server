package quest.rentus_base;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author maddison
 */
public class _30503RodelionRescue extends AbstractQuestHandler {

	public _30503RodelionRescue() {
		super(30503);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205438).addOnQuestStart(questId);
		qe.registerQuestNpc(205438).addOnTalkEvent(questId);
		qe.registerQuestNpc(799541).addOnTalkEvent(questId);
		qe.registerQuestNpc(701097).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 205438) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					default:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799541) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SET_SUCCEED:
						changeQuestStep(env, 0, 0, true);
						return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205438) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

}
