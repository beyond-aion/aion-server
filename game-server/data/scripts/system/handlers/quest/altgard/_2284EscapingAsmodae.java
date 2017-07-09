package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _2284EscapingAsmodae extends AbstractQuestHandler {

	public _2284EscapingAsmodae() {
		super(2284);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203645).addOnQuestStart(questId);
		qe.registerOnLogOut(questId);
		qe.registerQuestNpc(798040).addOnTalkEvent(questId);
		qe.registerQuestNpc(798041).addOnTalkEvent(questId);
		qe.registerQuestNpc(798034).addOnTalkEvent(questId);
		qe.registerQuestNpc(203645).addOnTalkEvent(questId);
		qe.registerAddOnReachTargetEvent(questId);
		qe.registerAddOnLostTargetEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203645) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798040) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 798041) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO3) {
					return defaultStartFollowEvent(env, (Npc) env.getVisibleObject(), 798034, 1, 2);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798034) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				changeQuestStep(env, 2, 1);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onNpcReachTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 2, 2, true); // reward
	}

	@Override
	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 2, 1, false); // 1
	}
}
