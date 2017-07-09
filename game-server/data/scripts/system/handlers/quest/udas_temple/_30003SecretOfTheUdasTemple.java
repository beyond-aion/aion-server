package quest.udas_temple;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _30003SecretOfTheUdasTemple extends AbstractQuestHandler {

	public _30003SecretOfTheUdasTemple() {
		super(30003);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799029).addOnQuestStart(questId);
		qe.registerQuestNpc(799029).addOnTalkEvent(questId);
		qe.registerOnGetItem(182209161, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799029) { // Honeus
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 799029) { // Honeus
				if (dialogActionId == QUEST_SELECT) {
					if (var == 1) {
						return sendQuestDialog(env, 2375);
					}
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					changeQuestStep(env, 1, 1, true); // reward
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799029) { // Honeus
				removeQuestItem(env, 182209161, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 0, 1, false); // 1
	}
}
