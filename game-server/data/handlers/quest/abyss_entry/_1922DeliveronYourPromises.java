package quest.abyss_entry;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Hellboy, aion4Free, Gigi, Rolandas, vlog
 */
public class _1922DeliveronYourPromises extends AbstractQuestHandler {

	public _1922DeliveronYourPromises() {
		super(1922);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerQuestNpc(203830).addOnTalkEvent(questId);
		qe.registerQuestNpc(203901).addOnTalkEvent(questId);
		qe.registerQuestNpc(203764).addOnTalkEvent(questId);
		qe.registerQuestNpc(213582).addOnKillEvent(questId);
		qe.registerQuestNpc(213580).addOnKillEvent(questId);
		qe.registerQuestNpc(213581).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203830: // Fuchsia
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO12:
							return defaultCloseDialog(env, 0, 4); // 4
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				case 203901: // Telemachus
					switch (env.getDialogActionId()) {
						case USE_OBJECT:
							if (var == 7)
								return sendQuestDialog(env, 3739);
							return false;
						// Should be removed (it's for backward compatibility)
						// Epeios sets REWARD status now and doesn't reach this part
						case SELECT_QUEST_REWARD:
							if (var == 7) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 6);
							}
					}
					break;
				case 203764: // Epeios
					switch (env.getDialogActionId()) {
						case USE_OBJECT:
							if (var == 6) {
								return sendQuestDialog(env, 1779);
							}
							return false;
						case QUEST_SELECT:
							if (var == 4) {
								return sendQuestDialog(env, 1693);
							} else if (qs.getQuestVarById(4) == 10) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO3:
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.SANCTUM_UNDERGROUND_ARENA.getId(), player);
							TeleportService.teleportTo(player, newInstance, 276, 293, 163, (byte) 90, TeleportAnimation.NONE);
							if (var == 4 || var == 6)
								changeQuestStep(env, var, 5, false); // 5
							return closeDialogWindow(env);
						case SETPRO4:
							qs.setQuestVarById(0, 7);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return defaultCloseDialog(env, 7, 7); // 7
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203901) { // Telemachus
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 3739);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 6);
				else {
					qs.setRewardGroup(1); // always reward group 1 since other choices than arena are not available anymore
					return sendQuestEndDialog(env);
				}
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
			if (var == 5) {
				int var4 = qs.getQuestVarById(4);
				int[] mobs = { 213580, 213581, 213582 };
				if (var4 < 9) {
					return defaultOnKillEvent(env, mobs, 0, 9, 4); // 4: 1 - 9
				} else if (var4 == 9) {
					defaultOnKillEvent(env, mobs, 9, 10, 4); // 4: 10
					QuestService.questTimerEnd(env);
					playQuestMovie(env, 166);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var4 = qs.getQuestVarById(4);
			if (var4 < 10) {
				qs.setQuestVar(6);
				updateQuestStatus(env);
				TeleportService.teleportTo(player, 110010000, 1466.036f, 1337.2749f, 566.41583f, (byte) 86);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int var4 = qs.getQuestVars().getVarById(4);
			if (var == 5 && var4 != 10) {
				if (player.getWorldId() != 310080000) {
					QuestService.questTimerEnd(env);
					qs.setQuestVar(6);
					updateQuestStatus(env);
					return true;
				} else {
					playQuestMovie(env, 165);
					QuestService.questTimerStart(env, 240);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 166) {
			TeleportService.teleportTo(env.getPlayer(), 110010000, 1466.036f, 1337.2749f, 566.41583f, (byte) 86);
		} else if (movieId == 165) {
			QuestService.questTimerStart(env, 240);
		}
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 1921);
	}
}
