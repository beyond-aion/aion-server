package quest.abyss_entry;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * Speak to Telemachus (203901). Talk with Daedalus (203930) about your flight test. Go through all the rings within the time limit. Talk with
 * Daedalus again. Report to Telemachus.
 * 
 * @author Hellboy, aion4Free, Hilgert, vlog
 */
public class _1044TestingFlightSkills extends AbstractQuestHandler {

	private String[] rings = { "ELTNEN_FORTRESS_210020000_1", "ELTNEN_FORTRESS_210020000_2", "ELTNEN_FORTRESS_210020000_3",
		"ELTNEN_FORTRESS_210020000_4", "ELTNEN_FORTRESS_210020000_5", "ELTNEN_FORTRESS_210020000_6" };

	public _1044TestingFlightSkills() {
		super(1044);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerQuestNpc(203901).addOnTalkEvent(questId);
		qe.registerQuestNpc(203930).addOnTalkEvent(questId);
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
				case 203901: // Telemachus
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
				case 203930: // Daedalus
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var >= 2 && var <= 7) {
								return sendQuestDialog(env, 3143);
							} else if (var == 9) {
								return sendQuestDialog(env, 1693);
							} else if (var == 10) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case SELECT2_1_1:
							if (var == 1 || var == 10) {
								playQuestMovie(env, 40);
								return sendQuestDialog(env, 1354);
							}
							return false;
						case SETPRO2:
							if (var == 1) {
								QuestService.questTimerStart(env, 110);
								return defaultCloseDialog(env, 1, 2); // 2
							} else if (var == 10) {
								QuestService.questTimerStart(env, 110);
								return defaultCloseDialog(env, 10, 2); // 2
							}
							return false;
						case SET_SUCCEED:
							if (var == 9) {
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
			if (targetId == 203901) { // Telemachus
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
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 1922);
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
				changeQuestStep(env, 7, 9); // 9
				if (qs.getQuestVarById(0) == 9)
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
				changeQuestStep(env, var, 10); // 10
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
}
