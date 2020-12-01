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
public class _4906TalesOfHeroes extends AbstractQuestHandler {

	public _4906TalesOfHeroes() {
		super(4906);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204211).addOnQuestStart(questId);
		qe.registerQuestNpc(204211).addOnTalkEvent(questId);
		qe.registerQuestNpc(205188).addOnTalkEvent(questId);
		qe.registerQuestNpc(278003).addOnTalkEvent(questId);
		qe.registerQuestNpc(204057).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204211) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, 182207075, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205188) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1352);
					}
				} else if (dialogActionId == SETPRO1) {
					removeQuestItem(env, 182207075, 1);
					giveQuestItem(env, 182207076, 1);
					return defaultCloseDialog(env, 0, 1);
				}
			}
			if (targetId == 278003) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1) {
						return sendQuestDialog(env, 1693);
					}
				} else if (dialogActionId == SETPRO2) {
					removeQuestItem(env, 182207076, 1);
					giveQuestItem(env, 182207077, 1);
					return defaultCloseDialog(env, 1, 2);
				}
			}
			if (targetId == 204057) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2) {
						return sendQuestDialog(env, 2034);
					}
				} else if (dialogActionId == SETPRO3) {
					removeQuestItem(env, 182207077, 1);
					giveQuestItem(env, 182207078, 1);
					qs.setQuestVar(3);
					return defaultCloseDialog(env, 3, 3, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204211) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				removeQuestItem(env, 182207078, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
