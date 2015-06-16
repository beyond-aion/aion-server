package quest.altgard;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author Artur
 */
public class _24016AStrangeNewThread extends QuestHandler {

	private final static int questId = 24016;
	private final static int[] npcs = { 203557, 700140 };

	public _24016AStrangeNewThread() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnDie(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnMovieEndQuest(154, questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerQuestNpc(214103).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203557: { // Suthran
					if (env.getDialog() == DialogAction.QUEST_SELECT && var == 0) {
						return sendQuestDialog(env, 1011);
					}
					else if (env.getDialog() == DialogAction.SETPRO1) {
						TeleportService2.teleportTo(player, 220030000, 2453.1934f, 2555.148f, 316.267f);
						changeQuestStep(env, 0, 1, false); // 1
						return closeDialogWindow(env);
					}
					else if (env.getDialogId() == DialogAction.SELECT_ACTION_1013.id()) {
						playQuestMovie(env, 66);
						return sendQuestDialog(env, 1013);
					}
					break;
				}
				case 700140: { // Gate Guardian Stone
					if (var == 2) {
						if (env.getDialog() == DialogAction.USE_OBJECT) {
							QuestService.addNewSpawn(320030000, player.getInstanceId(), 214103, (float) 260.12, (float) 234.93,
								(float) 216.00, (byte) 90);
							return useQuestObject(env, 2, 3, false, false); // 3
						}
					}
					else if (var == 4) {
						if (env.getDialog() == DialogAction.USE_OBJECT) {
							//return playQuestMovie(env, 154); something wrong with movieendevent,quest is not switched to reward state.
							changeQuestStep(env, 4, 5, true); // reward
							TeleportService2.teleportTo(env.getPlayer(), 220030000, 2453.1934f, 2555.148f, 316.267f);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203557) { // Suthran
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				}
				else {
					return sendQuestEndDialog(env);
				}
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
			if (var >= 2 && player.getWorldId() != 320030000) {
				changeQuestStep(env, var, 1, false);
				return true;
			}
			else if (var == 1 && player.getWorldId() == 320030000) {
				changeQuestStep(env, 1, 2, false); // 2
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 214103, 3, 4); // 4
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var >= 2) {
				qs.setQuestVar(1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (movieId == 154) {
				qs.setQuestVar(4);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				TeleportService2.teleportTo(env.getPlayer(), 220030000, 2453.1934f, 2555.148f, 316.267f);
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
		return defaultOnLvlUpEvent(env, 24010, true);
	}
}
