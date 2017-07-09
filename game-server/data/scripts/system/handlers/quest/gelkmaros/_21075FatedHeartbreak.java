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
public class _21075FatedHeartbreak extends AbstractQuestHandler {

	public _21075FatedHeartbreak() {
		super(21075);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799409).addOnQuestStart(questId);
		qe.registerQuestNpc(799409).addOnTalkEvent(questId);
		qe.registerQuestNpc(798392).addOnTalkEvent(questId);
		qe.registerQuestNpc(799410).addOnTalkEvent(questId);
		qe.registerQuestNpc(204138).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799409) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798392) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (dialogActionId == SETPRO1) {
					giveQuestItem(env, 182207917, 1);
					return defaultCloseDialog(env, 0, 1);
				} else if (dialogActionId == SETPRO2) {
					giveQuestItem(env, 182207917, 1);
					return defaultCloseDialog(env, 0, 2);
				}
			} else if (targetId == 799410) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SET_SUCCEED) {
					qs.setRewardGroup(0);
					removeQuestItem(env, 182207917, 1);
					return defaultCloseDialog(env, 1, 1, true, false);
				}
			} else if (targetId == 204138) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2)
						return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SET_SUCCEED) {
					qs.setRewardGroup(1);
					removeQuestItem(env, 182207917, 1);
					return defaultCloseDialog(env, 2, 2, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799409) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
