package quest.eltnen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Rhys2002
 * @reworked vlog
 */
public class _1038TheShadowsCommand extends QuestHandler {

	private final static int questId = 1038;

	public _1038TheShadowsCommand() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 203933, 700172, 203991, 700162 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnMovieEndQuest(35, questId);
		qe.registerQuestNpc(204005).addOnKillEvent(questId);
		qe.addHandlerSideQuestDrop(questId, 700172, 182201007, 1, 100, 2);
		qe.registerGetingItem(182201007, questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		if (defaultOnKillEvent(env, 204005, 7, true)) { // reward
			QuestService.questTimerEnd(env);
			return true;
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700162: { // Underground Temple Artifact
					if (dialog == DialogAction.USE_OBJECT) {
						return useQuestObject(env, 0, 1, false, 0, 34); // 1 + movie
					}
					break;
				}
				case 203933: { // Actaeon
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 3) {
								return sendQuestDialog(env, 1694);
							} else if (var == 4) {
								return sendQuestDialog(env, 2034);
							} else if (var == 5) {
								return sendQuestDialog(env, 2035);
							}
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 4, 5, false, 2035, 2120); // 5
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case SETPRO3: {
							removeQuestItem(env, 182201015, 1);
							removeQuestItem(env, 182201016, 1);
							removeQuestItem(env, 182201017, 1);
							return defaultCloseDialog(env, 3, 4, 0, 0, 182201007, 1); // 4
						}
						case SETPRO4: {
							return defaultCloseDialog(env, 5, 6); // 6
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
					break;
				}
				case 700172: { // Philipemos's Corpse
					if (var == 2) {
						if (dialog == DialogAction.USE_OBJECT) {
							return true; // loot
						}
					}
					break;
				}
				case 203991: { // Dionera
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 6) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO5: {
							playQuestMovie(env, 35);
							return defaultCloseDialog(env, 6, 7); // 7
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203991) { // Dionera
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 2, 3, false); // 3
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 7) {
				changeQuestStep(env, 7, 6, false); // 6
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 35) {
			QuestService.questTimerStart(env, 180);
			QuestService.addNewSpawn(210020000, 1, 204005, (float) 1768.16, (float) 924.47, (float) 422.02, (byte) 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1300, true);
	}
}
