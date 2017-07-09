package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _2758CarryTheFlame extends AbstractQuestHandler {

	public _2758CarryTheFlame() {
		super(2758);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(279000).addOnQuestStart(questId);
		qe.registerQuestNpc(790016).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 279000) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 279000) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				if (env.getDialogActionId() == SETPRO1) {
					giveQuestItem(env, 182205645, 1);
					QuestService.questTimerStart(env, 900);
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 790016) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (env.getDialogActionId() == SET_SUCCEED) {
					QuestService.questTimerEnd(env);
					removeQuestItem(env, 182205645, 1);
					qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVar(1);
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790016) {
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 5);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var > 1) {
				removeQuestItem(env, 182205645, 1);
				changeQuestStep(env, var, 0);
				return true;
			}
		}
		return false;
	}
}
