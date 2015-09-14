package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * Talk with Spatalos (203098).<br>
 * Enter the Sealed Space via the Abyss Gate in Kraka's Den.<br>
 * Obtain the Seal of Kuninasha (182200024) and destroy the Abyss Gate(700141).<br>
 * Report to Spatalos.
 * 
 * @author Atomics
 * @reworked vlog
 */
public class _1020SealingTheAbyssGate extends QuestHandler {

	private final static int questId = 1020;

	public _1020SealingTheAbyssGate() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 203098, 700142 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		qe.registerQuestNpc(210753).addOnKillEvent(questId);
		qe.registerOnMovieEndQuest(153, questId);
		for (int npcId : npcs) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203098: { // Spatalos
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							TeleportService2.teleportTo(player, 210030000, 2683.2085f, 1068.8977f, 199.375f);
							changeQuestStep(env, 0, 1, false); // 1
							return closeDialogWindow(env);
						}
					}
					break;
				}
				case 700142: { // Abyss Gate Guardian Stone
					if (dialog == DialogAction.USE_OBJECT) {
						if (QuestService.collectItemCheck(env, true)) {
							return playQuestMovie(env, 153);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203098) { // Spatalos
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 2) {
				changeQuestStep(env, 2, 1, false);
				removeQuestItem(env, 182200024, 1);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 2 && player.getWorldId() != 310030000) {
				changeQuestStep(env, 2, 1, false);
				removeQuestItem(env, 182200024, 1);
				return true;
			} else if (var == 1 && player.getWorldId() == 310030000) {
				changeQuestStep(env, 1, 2, false); // 2
				QuestService.addNewSpawn(310030000, player.getInstanceId(), 210753, (float) 258.89917, (float) 237.20166, (float) 217.06035, (byte) 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				if (env.getTargetId() == 210753) {
					if (player.getInventory().getItemCountByItemId(182200024) < 1) {
						return giveQuestItem(env, 182200024, 1);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (movieId == 153) {
				changeQuestStep(env, 2, 2, true); // reward
				TeleportService2.teleportTo(env.getPlayer(), 210030000, 2683.2085f, 1068.8977f, 199.375f);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] verteronQuests = { 1130, 1011, 1012, 1013, 1014, 1015, 1021, 1016, 1018, 1017, 1019, 1022, 1023 };
		return defaultOnLvlUpEvent(env, verteronQuests, true);
	}
}
