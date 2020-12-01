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
public class _2919BookOfOblivion extends AbstractQuestHandler {

	public _2919BookOfOblivion() {
		super(2919);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204206).addOnQuestStart(questId);
		qe.registerQuestNpc(204206).addOnTalkEvent(questId);
		qe.registerQuestNpc(204215).addOnTalkEvent(questId);
		qe.registerQuestNpc(204192).addOnTalkEvent(questId);
		qe.registerQuestNpc(700212).addOnTalkEvent(questId);
		qe.registerQuestNpc(204224).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204206) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204215) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 204192) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO3) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 700212) {
				if (dialogActionId == USE_OBJECT) {
					if (qs.getQuestVarById(0) == 2)
						return sendQuestDialog(env, 2034);
					else if (qs.getQuestVarById(0) == 6)
						return sendQuestDialog(env, 3057);
				} else if (dialogActionId == SETPRO4) {
					changeQuestStep(env, 2, 3);
					return closeDialogWindow(env);
				} else if (dialogActionId == SETPRO7) {
					giveQuestItem(env, 182207013, 1);
					changeQuestStep(env, 6, 7);
					return closeDialogWindow(env);
				}
			} else if (targetId == 204206) {
				if (qs.getQuestVarById(0) == 7) {
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 3398);
					}
				}
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 3)
						return sendQuestDialog(env, 2375);
				} else if (dialogActionId == SETPRO5) {
					return defaultCloseDialog(env, 3, 4);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					removeQuestItem(env, 182207013, 1);
					return defaultCloseDialog(env, 7, 7, true, true);
				}
			} else if (targetId == 204224) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 4)
						return sendQuestDialog(env, 2716);
				} else if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 4, 6, false, 2802, 2717);
				} else if (dialogActionId == SETPRO6) {
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204206) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 5);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
