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
public class _21455IngredientsForTheAntidote extends AbstractQuestHandler {

	public _21455IngredientsForTheAntidote() {
		super(21455);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799404).addOnQuestStart(questId);
		qe.registerQuestNpc(799240).addOnTalkEvent(questId);
		qe.registerQuestNpc(799244).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799404) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182209514, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799240) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO1) {
					qs.setQuestVar(1);
					removeQuestItem(env, 182209514, 1);
					giveQuestItem(env, 182209515, 1);
					return defaultCloseDialog(env, 1, 1, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799244) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				removeQuestItem(env, 182209515, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
