package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _4205SmackTheShulack extends AbstractQuestHandler {

	public _4205SmackTheShulack() {
		super(4205);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(279010).addOnQuestStart(questId);
		qe.registerQuestNpc(279010).addOnTalkEvent(questId);
		qe.registerQuestNpc(204202).addOnTalkEvent(questId);
		qe.registerQuestNpc(204285).addOnTalkEvent(questId);
		qe.registerQuestNpc(218972).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 279010) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 279010) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 15) {
						return sendQuestDialog(env, 1352);
					}
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 15, 16);
				}
			} else if (targetId == 204202) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 16)
							return sendQuestDialog(env, 1693);
						return false;
					case SET_SUCCEED:
						if (var == 16)
							changeQuestStep(env, 16, 16, true);
						return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204285) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 218972, 0, 15);
	}
}
