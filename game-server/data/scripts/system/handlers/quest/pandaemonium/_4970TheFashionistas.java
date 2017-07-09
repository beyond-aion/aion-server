package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _4970TheFashionistas extends AbstractQuestHandler {

	public _4970TheFashionistas() {
		super(4970);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799315).addOnQuestStart(questId);
		qe.registerQuestNpc(798392).addOnTalkEvent(questId);
		qe.registerQuestNpc(798393).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799315) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182206120, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798392) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1352);
					}
				} else if (dialogActionId == SETPRO1) {
					qs.setQuestVar(1);
					return defaultCloseDialog(env, 1, 1, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798393) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				removeQuestItem(env, 182206120, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
