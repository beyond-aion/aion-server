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
public class _21136InSearchOfAWitness extends AbstractQuestHandler {

	public _21136InSearchOfAWitness() {
		super(21136);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799271).addOnQuestStart(questId);
		qe.registerQuestNpc(799413).addOnTalkEvent(questId);
		qe.registerQuestNpc(799414).addOnTalkEvent(questId);
		qe.registerQuestNpc(730355).addOnTalkEvent(questId);
		qe.registerCanAct(getQuestId(), 730355);
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
					return sendQuestStartDialog(env, 182207919, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799413) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO1) {
					removeQuestItem(env, 182207919, 1);
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 799414) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO2) {
					qs.setQuestVar(2);
					return defaultCloseDialog(env, 2, 2, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 730355) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
