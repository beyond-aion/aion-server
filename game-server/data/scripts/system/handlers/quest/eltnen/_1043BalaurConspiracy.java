package quest.eltnen;

import java.util.List;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author Balthazar
 * @modified Pad
 */

public class _1043BalaurConspiracy extends QuestHandler {

	private final static int questId = 1043;
	private final static int mobId = 213575; // Crusader
	private final static int[] npcIds = { 203901, 204020, 204044 };
	private int balaurKilled = 0;

	public _1043BalaurConspiracy() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnEnterWorld(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		qe.registerQuestNpc(mobId).addOnKillEvent(questId);
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
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203901) { // Telemachus
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 2375);
				else
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203901: { // Telemachus
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
						case SETPRO1:
							defaultCloseDialog(env, 0, 1); // 1
							TeleportService2.teleportTo(player, 210020000, 1596.1948f, 1529.9152f, 317, (byte) 120, TeleportAnimation.FADE_OUT_BEAM);
							return true;
					}
					break;
				}
				case 204020: { // Mabangtah
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
						case SETPRO2:
							defaultCloseDialog(env, 1, 2); // 2
							TeleportService2.teleportTo(player, 210020000, 2500.15f, 780.9f, 409, (byte) 15, TeleportAnimation.FADE_OUT_BEAM);
							return true;
					}
					break;
				}
				case 204044: { // Kimeia
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
							else if (var == 4)
								return sendQuestDialog(env, 2034);
						case SETPRO3: {
							balaurKilled = 0;
							QuestService.spawnQuestNpc(310040000, player.getInstanceId(), mobId, 248.78f, 259.28f, 227.74f, (byte) 94);
							QuestService.spawnQuestNpc(310040000, player.getInstanceId(), mobId, 259.10f, 261.79f, 227.77f, (byte) 94);
							QuestService.questTimerStart(env, 240);
							return defaultCloseDialog(env, 2, 3); // 3
						}
						case SETPRO4:
							if (var == 4) {
								defaultCloseDialog(env, 4, 4, true, false); // reward
								TeleportService2.teleportTo(player, 210020000, 271.69f, 2787.04f, 272.47f, (byte) 50, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
					}
					break;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3) {
			if (env.getTargetId() == mobId) {
				balaurKilled++;
				if (balaurKilled == 2) {
					QuestService.spawnQuestNpc(310040000, player.getInstanceId(), mobId, 248.78f, 259.28f, 227.74f, (byte) 94);
					QuestService.spawnQuestNpc(310040000, player.getInstanceId(), mobId, 259.10f, 261.79f, 227.77f, (byte) 94);
				} else if (balaurKilled == 4) {
					QuestService.questTimerEnd(env);
					if (kimeiaIsAlive(env)) {
						changeQuestStep(env, 3, 4, false);
						playQuestMovie(env, 157);
					} else {
						changeQuestStep(env, 3, 2, false);
						QuestService.spawnQuestNpc(310040000, player.getInstanceId(), 204044, 272.83f, 176.81f, 204.35f, (byte) 0);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3) {
			deleteBalaur(env);
			if (kimeiaIsAlive(env)) {
				changeQuestStep(env, 3, 4, false);
				playQuestMovie(env, 157);
			} else {
				changeQuestStep(env, 3, 2, false);
				QuestService.spawnQuestNpc(310040000, player.getInstanceId(), 204044, 272.83f, 176.81f, 204.35f, (byte) 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3) {
			deleteBalaur(env);
			QuestService.questTimerEnd(env);
			changeQuestStep(env, 3, 2, false);
			if (!kimeiaIsAlive(env))
				QuestService.spawnQuestNpc(310040000, player.getInstanceId(), 204044, 272.83f, 176.81f, 204.35f, (byte) 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getQuestVarById(0) == 3 && player.getWorldId() != 310040000) {
			QuestService.questTimerEnd(env);
			changeQuestStep(env, 3, 2, false);
			return true;
		}
		return false;
	}

	private boolean kimeiaIsAlive(QuestEnv env) {
		Npc kimeia = env.getPlayer().getPosition().getWorldMapInstance().getNpc(204044);
		if (kimeia != null && !kimeia.getLifeStats().isAlreadyDead())
			return true;
		return false;
	}

	private void deleteBalaur(QuestEnv env) {
		List<Npc> npcs = env.getPlayer().getPosition().getWorldMapInstance().getNpcs(mobId);
		for (Npc npc : npcs) {
				npc.getController().onDelete();
		}
	}
}
