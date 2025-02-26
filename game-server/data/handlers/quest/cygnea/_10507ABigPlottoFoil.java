package quest.cygnea;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 * @Description
 * 							Talk with Aithria at Aequis Detachment Post.
 *              Talk with Pluvis at Aequis Detachment Post.
 *              Talk with Pruina at Aequis Detachment Post.
 *              Talk with Ricoro at Aequis Detachment Post.
 *              Talk with Aithria at Aequis Detachment Post.
 *              Talk with Nostibas who has been dispatched to Wailing Dunes.
 *              Create a diversion for the Beritra Frontier Units of the Beritra Legion. Eliminate Beritra Guards (5). Destroy Beritra Supplies Boxes
 *              (3)
 *              Infiltrate Beritra Guard Captain's Tent.
 *              Report back to Aithria at Aequis Detachment Post.
 *              See Aithria and prepare for an all-out battle against Beritra.
 */
public class _10507ABigPlottoFoil extends AbstractQuestHandler {

	public _10507ABigPlottoFoil() {
		super(10507);
	}

	@Override
	public void register() {
		// Aithria 804711
		// Pluvis 804712
		// Pruina 804713
		// Ricoro 804714
		// Nostibas 804715
		int[] npcs = { 804711, 804712, 804713, 804714, 804715 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}

		// Beritra Guard 236264 236265
		// Beritra Supplies Box 702668
		int[] mobs = { 236264, 236265, 702668 };
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerOnEnterZone(ZoneName.get("LF5_SENSORYAREA_Q10507_206366_2_210070000"), questId); // Beritra Guard Captain's Tent zone
		qe.registerOnLevelChanged(questId);
		qe.registerOnQuestCompleted(questId);
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

		switch (targetId) {
			case 804711: // Aithria
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Aithria at Aequis Detachment Post.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1011);
						}

						if (dialogActionId == SETPRO1) {
							return defaultCloseDialog(env, var, var + 1);
						}
					}

					if (var == 4) { // Step 4: Talk with Aithria at Aequis Detachment Post.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 2375);
						}

						if (dialogActionId == SETPRO5) {
							return defaultCloseDialog(env, var, var + 1);
						}
					}
				}

				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}
					return sendQuestEndDialog(env);
				}
				break;
			case 804712: // Pluvis
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Talk with Pluvis at Aequis Detachment Post.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1352);
						}

						if (dialogActionId == SETPRO2) {
							return defaultCloseDialog(env, var, var + 1); // 1
						}
					}
				}
				break;
			case 804713: // Pruina
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 2) { // Step 2: Talk with Pruina at Aequis Detachment Post.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1693);
						}

						if (dialogActionId == SETPRO3) {
							return defaultCloseDialog(env, var, var + 1); // 1
						}
					}
				}
				break;
			case 804714: // Ricoro
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 3) { // Step 3: Talk with Ricoro at Aequis Detachment Post.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}

						if (dialogActionId == SETPRO4) {
							return defaultCloseDialog(env, var, var + 1); // 1
						}
					}
				}
				break;
			case 804715: // Nostibas
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 5) { // Step 5:
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 2716);
						}

						if (dialogActionId == SETPRO6) {
							return defaultCloseDialog(env, var, var + 1); // 1
						}
					}
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) { // Step 7: Infiltrate Beritra Guard Captain's Tent.

		if (zoneName == ZoneName.get("LF5_SENSORYAREA_Q10507_206366_2_210070000")) {

			Player player = env.getPlayer();
			if (player == null) {
				return false;
			}

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				if (var == 7) {
					qs.setQuestVar(var + 1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					playQuestMovie(env, 993);
					player.getMoveController().abortMove();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (var == 6) { // Step 6: Eliminate Beritra Guards (5). Destroy Beritra Supplies Boxes (3)
			int varKill1 = qs.getQuestVarById(1); // Kill counter var 1
			int varKill2 = qs.getQuestVarById(2); // Kill counter var 2
			if (targetId == 702668 && varKill2 <= 2) { // Beritra Supplies Box
				qs.setQuestVarById(2, varKill2 + 1); // 1-3 with varNum 2
				updateQuestStatus(env);
			} else if ((targetId == 236264 || targetId == 236265) && varKill1 <= 4) { // Beritra Guards
				qs.setQuestVarById(1, varKill1 + 1); // 1-5 with varNum 1
				updateQuestStatus(env);
			}

			if (varKill1 >= 4 && varKill2 >= 2) {
				qs.setQuestVar(var + 1);
				updateQuestStatus(env);
			}
			return true;
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId != 993)
			return;
		Player player = env.getPlayer();
		spawnTemporarily(236266, player.getWorldMapInstance(), player.getX() + 2, player.getY() + 3, player.getZ(), (byte) 73, 2);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10500);
	}
}
