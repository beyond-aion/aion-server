package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _1908UlaguruSpeaks extends AbstractQuestHandler {

	public _1908UlaguruSpeaks() {
		super(1908);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203864).addOnQuestStart(questId);
		qe.registerQuestNpc(203864).addOnTalkEvent(questId);
		qe.registerQuestNpc(204120).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203864) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203890) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1352);
					}
				} else if (dialogActionId == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 203864) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1) {
						return sendQuestDialog(env, 2375);
					}
				} else if (dialogActionId == SETPRO21) {
					qs.setQuestVar(21);
					qs.setRewardGroup(0);
					return sendQuestDialog(env, 2376);
				} else if (dialogActionId == SETPRO22) {
					qs.setQuestVar(22);
					qs.setRewardGroup(1);
					return sendQuestDialog(env, 2461);
				} else if (dialogActionId == SETPRO23) {
					qs.setQuestVar(23);
					qs.setRewardGroup(2);
					return sendQuestDialog(env, 2546);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203864) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
