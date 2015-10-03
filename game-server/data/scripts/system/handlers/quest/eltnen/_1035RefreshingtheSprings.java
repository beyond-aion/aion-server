package quest.eltnen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * Talk with Gaia (203917). Talk with Ophelos (203992). Retrieve a Life Bead (182201014) and put it into the Laquepin Life Stone (700158). Talk with
 * Ophelos. Talk with Castor (203965). Talk with Corybantes (203968). Talk with Heratos (203987). Go to the Desert Life Stone (700160) and insert the
 * Life Bead (182201024) within three minutes. Talk with Heratos. Talk with Sirink (203934). Restore the Mystic Spring by putting the Bead (182201025)
 * into the Temple Life Stone (700159). Talk with Sirink. Talk with Gaia.
 * 
 * @author Rhys2002
 * @reworked vlog
 */
public class _1035RefreshingtheSprings extends QuestHandler {

	private final static int questId = 1035;
	private final static int[] npc_ids = { 203917, 203992, 700158, 203965, 203968, 203987, 700160, 203934, 700159 };

	public _1035RefreshingtheSprings() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnMovieEndQuest(31, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
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

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203917) { // Gaia
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 4080);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203917: { // Gaia
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
				case 203992: { // Ophelos
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 3) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case SETPRO3: {
							if (defaultCloseDialog(env, 3, 4)) { // 4
								TeleportService2.teleportTo(player, 210020000, 1, 641.88605f, 440.1764f, 331.875f, (byte) 64, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
							return false;
						}
					}
					break;
				}
				case 700158: { // Laquepin Life Stone
					if (var == 2) {
						if (dialog == DialogAction.USE_OBJECT && player.getInventory().getItemCountByItemId(182201014) == 1) {
							return useQuestObject(env, 2, 3, false, 0, 0, 0, 182201014, 1); // 3
						}
					}
					break;
				}
				case 203965: { // Castor
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								return sendQuestDialog(env, 2034);
							}
						}
						case SETPRO4: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
				case 203968: { // Corybantes
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 5) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO5: {
							if (defaultCloseDialog(env, 5, 6)) {
								TeleportService2.teleportTo(player, 210020000, 1, 1059.5428f, 342.35223f, 306.9911f, (byte) 64, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
							return false; // 6
						}
					}
					break;
				}
				case 203987: { // Heratos
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 6) {
								return sendQuestDialog(env, 2716);
							} else if (var == 7) {
								return sendQuestDialog(env, 2887);
							} else if (var == 8) {
								return sendQuestDialog(env, 3057);
							}
						}
						case SETPRO6: {
							QuestService.questTimerStart(env, 180);
							giveQuestItem(env, 182201024, 1);
							return defaultCloseDialog(env, 6, 7); // 7
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
						case SETPRO7: {
							if (defaultCloseDialog(env, 8, 9, 182201025, 1, 0, 0)) { // 9
								TeleportService2.teleportTo(player, 210020000, 1, 1527.835f, 520.4294f, 356.45963f, (byte) 117, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
							return false;
						}
					}
					break;
				}
				case 700160: { // Desert Life Stone
					if (var == 7) {
						if (dialog == DialogAction.USE_OBJECT && player.getInventory().getItemCountByItemId(182201024) == 1) {
							return playQuestMovie(env, 31);
						}
					}
					break;
				}
				case 203934: { // Sirink
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 9) {
								return sendQuestDialog(env, 3398);
							} else if (var == 11) {
								return sendQuestDialog(env, 3739);
							}
						}
						case SETPRO8: {
							if (var == 9) {
								return defaultCloseDialog(env, 9, 10); // 10
							} else if (var == 11) {
								return defaultCloseDialog(env, 11, 11, true, false); // reward
							}
						}
					}
					break;
				}
				case 700159: { // Temple Life Stone
					if (var == 10) {
						if (dialog == DialogAction.USE_OBJECT && player.getInventory().getItemCountByItemId(182201025) >= 1) {
							return useQuestObject(env, 10, 11, false, 0, 0, 0, 182201025, 1); // 11
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 31) {
			int var = env.getPlayer().getQuestStateList().getQuestState(questId).getQuestVarById(0);
			if (var == 7) {
				changeQuestStep(env, 7, 8, false); // 8
			} else if (var == 6) { // If timer stopped before movie ends
				changeQuestStep(env, 6, 8, false); // 8
			}
			removeQuestItem(env, 182201024, 1);
			QuestService.questTimerEnd(env);
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
}
