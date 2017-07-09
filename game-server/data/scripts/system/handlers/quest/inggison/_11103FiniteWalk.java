package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _11103FiniteWalk extends AbstractQuestHandler {

	public _11103FiniteWalk() {
		super(11103);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798983).addOnQuestStart(questId);
		qe.registerQuestNpc(798983).addOnTalkEvent(questId);
		qe.registerQuestNpc(798963).addOnTalkEvent(questId);
		qe.registerQuestNpc(798973).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798983) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182206750, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798963) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 798973) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO2) {
					removeQuestItem(env, 182206750, 1);
					qs.setQuestVar(2);
					return defaultCloseDialog(env, 2, 2, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798983) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
