package quest.talocs_hollow;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _21467SpawningTheSapSuckers extends QuestHandler {

	public _21467SpawningTheSapSuckers() {
		super(21467);
	}

	@Override
	public void register() {
		qe.registerOnDie(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerQuestNpc(799527).addOnQuestStart(questId);
		qe.registerQuestNpc(799503).addOnTalkEvent(questId);
		qe.registerQuestNpc(215480).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799527) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else if (dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.questTimerStart(env, 480);
					return sendQuestStartDialog(env);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			int var = qs.getQuestVarById(0);
			int rewInd = 0;
			if (var == 1)
				rewInd = 1;
			else if (var == 2)
				rewInd = 2;
			else if (var == 3)
				rewInd = 3;
			if (targetId == 799503) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env, rewInd);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int targetId = env.getTargetId();
			int var = qs.getQuestVarById(0);
			if (targetId == 215480) {
				QuestService.questTimerEnd(env);
				changeQuestStep(env, var, var, true);
				return true;
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
			if (var == 0) {
				QuestService.questTimerStart(env, 240);
				changeQuestStep(env, var, 1);
			} else if (var == 1) {
				QuestService.questTimerStart(env, 240);
				changeQuestStep(env, var, 2);
			} else if (var == 2) {
				changeQuestStep(env, var, 3);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			QuestService.abandonQuest(player, questId);
			return true;
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			QuestService.abandonQuest(player, questId);
		}
		return false;
	}
}
