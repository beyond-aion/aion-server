package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.questEngine.task.QuestTasks;

/**
 * @author Ritsu
 */
public class _24053TheMaulingoftheMau extends AbstractQuestHandler {

	private final static int[] npc_ids = { 204787, 204795, 204796, 204700 };

	public _24053TheMaulingoftheMau() {
		super(24053);
	}

	@Override
	public void register() {
		qe.registerOnLogOut(questId);
		qe.registerAddOnReachTargetEvent(questId);
		qe.registerAddOnLostTargetEvent(questId);
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onNpcReachTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 3, 4, false, 253);
	}

	@Override
	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 3, 2, false);
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 3) {
				changeQuestStep(env, 3, 2);
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24050);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24050);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204700) {
				if (dialogActionId == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204787) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					else if (var == 4)
						return sendQuestDialog(env, 2375);
					return false;
				case SELECT1_1:
					playQuestMovie(env, 252);
					break;
				case SETPRO1:
					if (var == 0) {
						return defaultCloseDialog(env, 0, 1); // 1
					}
					return false;
				case SET_SUCCEED:
					if (var == 4) {
						return defaultCloseDialog(env, 4, 4, true, false); // reward
					}
			}
		} else if (targetId == 204795) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					return false;
				case SETPRO2:
					if (var == 1) {
						return defaultCloseDialog(env, 1, 2); // 2
					}
			}
		} else if (targetId == 204796) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 2)
						return sendQuestDialog(env, 1693);
					return false;
				case SETPRO3:
					if (var == 2) {
						Npc survivor = (Npc) spawnInFrontOf(204806, player);
						survivor.getAi().onCreatureEvent(AIEventType.FOLLOW_ME, player);
						player.getController().addTask(TaskId.QUEST_FOLLOW, QuestTasks.newFollowingToTargetCheckTask(env, survivor, 204813));
						return defaultCloseDialog(env, 2, 3);
					}
			}
		}
		return false;
	}
}
