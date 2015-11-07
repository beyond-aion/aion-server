package quest.danaria;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller
 */
public class _20093FalsityIsAWayOfLife extends QuestHandler {

	private final static int questId = 20093;

	private final static int[] mobs = { 230397, 230396, 230401, 230402 };

	public _20093FalsityIsAWayOfLife() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 800835, 800839, 800846, 800847, 701558, 800848, 800529, 801328 };
		qe.registerOnMovieEndQuest(856, questId);
		qe.registerOnMovieEndQuest(858, questId);
		qe.registerOnDie(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnLevelUp(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int targetId = env.getTargetId();
			for (int id : mobs) {
				if (targetId == id) {
					if (var == 3) {
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 800839, 150.91f, 145.64f, 124.43f, (byte) 43);
					}
				}
			}
		}
		return defaultOnKillEvent(env, mobs, 1, 4) || defaultOnKillEvent(env, mobs, 7, 10);
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (movieId == 856) {
				QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 800846, 137.46f, 158.9f, 120.73f, (byte) 113);
				return true;
			} else if (movieId == 858) {
				TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 107.37f, 145.05f, 125.69f, (byte) 105,
					TeleportAnimation.FADE_OUT_BEAM);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20092);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 800835) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 0) {
							return sendQuestDialog(env, 1011);
						}
					}
					case SETPRO1: {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300900000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 300900000, newInstance.getInstanceId(), 150.62f, 145.74f, 124.41f, (byte) 36,
							TeleportAnimation.FADE_OUT_BEAM);
						return defaultCloseDialog(env, 0, 1);
					}
				}
			} else if (targetId == 800839) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 4) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO3: {
						playQuestMovie(env, 856);
						Npc npc = (Npc) env.getVisibleObject();
						npc.getController().onDelete();
						return defaultCloseDialog(env, 4, 5);
					}
				}
			} else if (targetId == 800846) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 5) {
							return sendQuestDialog(env, 2034);
						}
					}
					case SETPRO4: {
						Npc npc = (Npc) env.getVisibleObject();
						npc.getController().onDelete();
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 800847, 106.22f, 141.24f, 112.17f, (byte) 0);
						return defaultCloseDialog(env, 5, 6);
					}
				}
			} else if (targetId == 701558) {
				switch (dialog) {
					case USE_OBJECT: {
						if (qs.getQuestVarById(0) == 6) {
							return sendQuestDialog(env, 2375);
						} else if (qs.getQuestVarById(0) == 11) {
							return sendQuestDialog(env, 3398);
						} else {
							return closeDialogWindow(env);
						}
					}
					case SETPRO5: {
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 230402, 117.04f, 136.029f, 112.17f, (byte) 53);
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 230401, 120.39f, 139.96f, 111.99f, (byte) 60);
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 230402, 117.97f, 144.26f, 112.17f, (byte) 66);
						return defaultCloseDialog(env, 6, 7);
					}
					case SETPRO8: {
						playQuestMovie(env, 858);
						removeQuestItem(env, 182215253, 1);
						for (VisibleObject obj : player.getKnownList().getKnownObjects().values()) {
							if (obj instanceof Npc && obj.getObjectTemplate().getTemplateId() == 701850) {
								obj.getController().onDelete();
							}
						}
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 730738, 128.97f, 138, 112.32f, (byte) 60);
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 801322, 121, 138.11f, 112.17f, (byte) 0);
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 206330, 107.15f, 143.3f, 126.6f, (byte) 0, 28);
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 800848, 107.04f, 140.6f, 125.69f, (byte) 30);
						return defaultCloseDialog(env, 11, 12);
					}
				}
			} else if (targetId == 800847) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 10) {
							return sendQuestDialog(env, 3057);
						}
					}
					case SETPRO7: {
						giveQuestItem(env, 182215253, 1);
						Npc npc = (Npc) env.getVisibleObject();
						npc.getController().onDelete();
						QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), 800847, 106.22f, 141.24f, 112.17f, (byte) 0);
						return defaultCloseDialog(env, 10, 11);
					}
				}
			} else if (targetId == 800848) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 12) {
							return sendQuestDialog(env, 3739);
						}
					}
					case SETPRO9: {
						TeleportService2.teleportTo(player, 600050000, 1, 421, 428f, 288.49f, (byte) 13, TeleportAnimation.FADE_OUT_BEAM);
						return defaultCloseDialog(env, 12, 13);
					}
				}
			} else if (targetId == 800529) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 13) {
							return sendQuestDialog(env, 4080);
						}
					}
					case SETPRO10: {
						return defaultCloseDialog(env, 13, 13, true, false);
					}
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801328) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
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
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (player.getWorldId() != 300900000) {
				if (var > 0 && var < 13) {
					qs.setQuestVarById(0, 0);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP(DataManager.QUEST_DATA.getQuestById(questId)
						.getName()));
					return true;
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
			int var = qs.getQuestVarById(0);
			if (var > 0 && var < 13) {
				qs.setQuestVar(0);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP(DataManager.QUEST_DATA.getQuestById(questId)
					.getName()));
				return true;
			}
		}
		return false;
	}
}
