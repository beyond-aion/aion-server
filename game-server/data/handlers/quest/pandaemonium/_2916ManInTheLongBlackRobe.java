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
public class _2916ManInTheLongBlackRobe extends AbstractQuestHandler {

	public _2916ManInTheLongBlackRobe() {
		super(2916);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204141).addOnQuestStart(questId);
		qe.registerQuestNpc(204141).addOnTalkEvent(questId);
		qe.registerQuestNpc(204152).addOnTalkEvent(questId);
		qe.registerQuestNpc(204150).addOnTalkEvent(questId);
		qe.registerQuestNpc(798033).addOnTalkEvent(questId);
		qe.registerQuestNpc(203673).addOnTalkEvent(questId);
		qe.registerQuestNpc(700211).addOnTalkEvent(questId);
		qe.registerQuestNpc(700211).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204141) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204152) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 204150) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 204151) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 2)
						return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO3) {
					return defaultCloseDialog(env, 2, 3);
				}
			} else if (targetId == 798033) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 3)
						return sendQuestDialog(env, 2375);
				} else if (dialogActionId == SETPRO4) {
					return defaultCloseDialog(env, 3, 4);
				}
			} else if (targetId == 203673) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 4)
						return sendQuestDialog(env, 2716);
				} else if (dialogActionId == SETPRO5) {
					return defaultCloseDialog(env, 4, 5);
				}
			} else if (targetId == 700211) {
				if (qs.getQuestVarById(0) == 6)
					return true;
			} else if (targetId == 204141) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 6)
						return sendQuestDialog(env, 3057);
				} else if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 6, 6, true, 5, 3143);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204141) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 5);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 5) {
				changeQuestStep(env, 5, 6);
				return true;
			}
		}
		return false;
	}
}
