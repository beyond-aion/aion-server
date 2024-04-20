package quest.abyss_entry;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * Talk with Aegir (204301). Talk with Yornduf (204319). Go through all the rings within the time limit. Talk with Yornduf. Report back to Aegir.
 * 
 * @author Hellboy, aion4Free, Hilgert, vlog
 */
public class _2042TheLastCheckpoint extends AbstractQuestHandler {

	private String[] rings = { "MORHEIM_ICE_FORTRESS_220020000_1", "MORHEIM_ICE_FORTRESS_220020000_2", "MORHEIM_ICE_FORTRESS_220020000_3",
		"MORHEIM_ICE_FORTRESS_220020000_4", "MORHEIM_ICE_FORTRESS_220020000_5", "MORHEIM_ICE_FORTRESS_220020000_6" };

	public _2042TheLastCheckpoint() {
		super(2042);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(204301).addOnTalkEvent(questId);
		qe.registerQuestNpc(204319).addOnTalkEvent(questId);
		for (String ring : rings) {
			qe.registerOnPassFlyingRings(ring, questId);
		}
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnDie(questId);
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204301: // Aegir
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case FINISH_DIALOG:
							return defaultCloseDialog(env, 0, 0);
					}
					break;
				case 204319: // Yornduf
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var >= 2 && var <= 7) {
								return sendQuestDialog(env, 3143);
							} else if (var == 8) {
								return sendQuestDialog(env, 1693);
							} else if (var == 9) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case SELECT2_1_1:
							if (var == 1 || var == 10) {
								playQuestMovie(env, 89);
								return sendQuestDialog(env, 1354);
							}
							return false;
						case SETPRO2:
							if (var == 1) {
								QuestService.questTimerStart(env, 70);
								return defaultCloseDialog(env, 1, 2); // 2
							} else if (var == 9) {
								QuestService.questTimerStart(env, 70);
								return defaultCloseDialog(env, 9, 2); // 2
							}
							return false;
						case SET_SUCCEED:
							if (var == 8) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestSelectionDialog(env);
							}
							return false;
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204301) { // Aegir
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onPassFlyingRingEvent(QuestEnv env, String flyingRing) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (rings[0].equals(flyingRing)) {
				changeQuestStep(env, 2, 3); // 3
				return true;
			} else if (rings[1].equals(flyingRing)) {
				changeQuestStep(env, 3, 4); // 4
				return true;
			} else if (rings[2].equals(flyingRing)) {
				changeQuestStep(env, 4, 5); // 5
				return true;
			} else if (rings[3].equals(flyingRing)) {
				changeQuestStep(env, 5, 6); // 6
				return true;
			} else if (rings[4].equals(flyingRing)) {
				changeQuestStep(env, 6, 7); // 7
				return true;
			} else if (rings[5].equals(flyingRing)) {
				changeQuestStep(env, 7, 8); // 8
				if (qs.getQuestVarById(0) == 8)
					QuestService.questTimerEnd(env);
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
			if (var > 1 && var < 8) {
				changeQuestStep(env, var, 9); // 9
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		QuestService.questTimerEnd(env);
		return onQuestTimerEndEvent(env);
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		QuestService.questTimerEnd(env);
		return onQuestTimerEndEvent(env);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 2947);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 2947);
	}
}
