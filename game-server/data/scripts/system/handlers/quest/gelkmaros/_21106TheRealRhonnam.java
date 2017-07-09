package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _21106TheRealRhonnam extends AbstractQuestHandler {

	public _21106TheRealRhonnam() {
		super(21106);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799271).addOnQuestStart(questId);
		qe.registerQuestNpc(799271).addOnTalkEvent(questId);
		qe.registerQuestNpc(799272).addOnTalkEvent(questId);
		qe.registerQuestNpc(799273).addOnTalkEvent(questId);
		qe.registerQuestNpc(799274).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799271) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799272) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 799273) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 799274) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO3) {
					qs.setQuestVar(3);
					return defaultCloseDialog(env, 3, 3, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799271) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
