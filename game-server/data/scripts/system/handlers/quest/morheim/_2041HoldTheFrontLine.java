package quest.morheim;

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
 * Talk with Aegir (204301). Meet Taisan (204403). Pass through Morheim Abyss Gate and talk with Kargate (204423). Protect Kargate from the Balaur:
 * Crusader(213575) and Draconute Scout(213576). Speak to Kargate. Report back to Aegir.
 * 
 * @author vlog
 * @reworked Pad
 */
public class _2041HoldTheFrontLine extends QuestHandler {

	private final static int questId = 2041;
	private final static int mobId = 213575;
	private final static int[] npcIds = { 204301, 204403, 204432 };
	private int balaurKilled = 0;

	public _2041HoldTheFrontLine() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnEnterWorld(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		qe.registerQuestNpc(mobId).addOnKillEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2300, true);
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

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204301) { // Aegir
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 2375);
				else
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204301: { // Aegir
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
						case SETPRO1:
							defaultCloseDialog(env, 0, 1); // 1
							TeleportService2.teleportTo(player, 220020000, 2794.55f, 477.6f, 265.65f, (byte) 40, TeleportAnimation.FADE_OUT_BEAM);
							return true;
					}
					break;
				}
				case 204403: { // Taisan
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
						case SETPRO2:
							defaultCloseDialog(env, 1, 2); // 2
							TeleportService2.teleportTo(player, 220020000, 3030.5f, 875.5f, 363.0f, (byte) 12, TeleportAnimation.FADE_OUT_BEAM);
							return true;
					}
					break;
				}
				case 204432: { // Kargate
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
							else if (var == 4)
								return sendQuestDialog(env, 2034);
						case SETPRO3: {
							balaurKilled = 0;
							QuestService.spawnQuestNpc(320040000, player.getInstanceId(), mobId, 248.78f, 259.28f, 227.74f, (byte) 94);
							QuestService.spawnQuestNpc(320040000, player.getInstanceId(), mobId, 259.10f, 261.79f, 227.77f, (byte) 94);
							QuestService.questTimerStart(env, 240);
							return defaultCloseDialog(env, 2, 3); // 3
						}
						case SETPRO4:
							if (var == 4) {
								defaultCloseDialog(env, 4, 4, true, false); // reward
								TeleportService2.teleportTo(player, 220020000, 3030.8676f, 875.6538f, 363.2065f, (byte) 73, TeleportAnimation.FADE_OUT_BEAM);
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
					QuestService.spawnQuestNpc(320040000, player.getInstanceId(), mobId, 248.78f, 259.28f, 227.74f, (byte) 94);
					QuestService.spawnQuestNpc(320040000, player.getInstanceId(), mobId, 259.10f, 261.79f, 227.77f, (byte) 94);
				} else if (balaurKilled == 4) {
					QuestService.questTimerEnd(env);
					if (kargateIsAlive(env)) {
						changeQuestStep(env, 3, 4, false);
						playQuestMovie(env, 158);
					} else {
						changeQuestStep(env, 3, 2, false);
						QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 204432, 272.83f, 176.81f, 204.35f, (byte) 0);
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
			if (kargateIsAlive(env)) {
				changeQuestStep(env, 3, 4, false);
				playQuestMovie(env, 158);
			} else {
				changeQuestStep(env, 3, 2, false);
				QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 204432, 272.83f, 176.81f, 204.35f, (byte) 0);
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
			if (!kargateIsAlive(env))
				QuestService.spawnQuestNpc(320040000, player.getInstanceId(), 204432, 272.83f, 176.81f, 204.35f, (byte) 0);
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

		if (qs.getQuestVarById(0) == 3 && player.getWorldId() != 320040000) {
			QuestService.questTimerEnd(env);
			changeQuestStep(env, 3, 2, false);
			return true;
		}
		return false;
	}

	private boolean kargateIsAlive(QuestEnv env) {
		Npc kargate = env.getPlayer().getPosition().getWorldMapInstance().getNpc(204432);
		if (kargate != null && !kargate.getLifeStats().isAlreadyDead())
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
