package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Majka
 */
public class _20034RescuetheReians extends AbstractQuestHandler {

	public _20034RescuetheReians() {
		super(20034);
	}

	@Override
	public void register() {
		// Rebirthing Chamber Exit ID: 700706
		// Destroyed Gargoyle ID: 730243
		// Ortiz ID: 799295
		// Fjoelnir ID: 799297
		// Anilmo ID: 799341
		// Brainwashed Reian ID: 799513 (var1)
		// Brainwashed Reian ID: 799514 (var2)
		// Brainwashed Reian ID: 799515 (var3)
		// Brainwashed Reian ID: 799516 (var4)
		int[] npcs = { 730243, 799295, 799297, 799341, 799513, 799514, 799515, 799516 };
		qe.registerOnDie(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(216592).addOnKillEvent(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnQuestCompleted(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				if (player.getWorldId() == 300150000) {
					qs.setQuestVar(3);
					updateQuestStatus(env);
				}
			} else if (var > 2 && var < 6) {
				if (player.getWorldId() != 300150000) {
					PacketSendUtility.sendPacket(player,
						SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP_QUEST(DataManager.QUEST_DATA.getQuestById(questId).getName()));
					qs.setQuestVar(2);
					updateQuestStatus(env);
				}
			} else if (var == 6) {
				if (player.getWorldId() == 220070000) {
					qs.setQuestVar(7);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}
		int var = qs.getQuestVars().getQuestVars();
		if (var >= 3 && var < 6) {
			qs.setQuestVar(2);
			updateQuestStatus(env);
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP_QUEST(DataManager.QUEST_DATA.getQuestById(questId).getName()));
		}

		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var >= 3 && var < 6) {
				PacketSendUtility.sendPacket(player,
					SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP_QUEST(DataManager.QUEST_DATA.getQuestById(questId).getName()));
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		// int var1 = qs.getQuestVarById(1);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		Npc npc = (Npc) player.getTarget();
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799295) { // Ortiz
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 799297) { // Fjoelnir
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 0) {
						return sendQuestDialog(env, 1011);
					}
					return false;
				case SETPRO1:
					giveQuestItem(env, 182215630, 1);
					return defaultCloseDialog(env, 0, 1); // 1

			}
		} else if (targetId == 799295) { // Ortiz
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 1) {
						return sendQuestDialog(env, 1352);
					}
					return false;
				case SETPRO2:
					removeQuestItem(env, 182215630, 1);
					giveQuestItem(env, 182215595, 1);
					return defaultCloseDialog(env, 1, 2); // 2
			}
		} else if (targetId == 730243) { // Destroyed Gargoyle
			switch (dialogActionId) {
				case USE_OBJECT:
					if (var == 2) {
						return sendQuestDialog(env, 1693);
					}
					return false;
				case SETPRO3:
					if (var == 2) {
						removeQuestItem(env, 182215595, 1);
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.UDAS_TEMPLE.getId(), player);
						TeleportService.teleportTo(player, newInstance, 561.8651f, 221.91483f, 134.53333f, (byte) 90,
							TeleportAnimation.FADE_OUT_BEAM);
						return true;
					}
			}
		} else if (targetId == 799513 || targetId == 799514 || targetId == 799515 || targetId == 799516) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 3) {
						int var1 = qs.getQuestVarById(1);
						int var2 = qs.getQuestVarById(2);
						int var3 = qs.getQuestVarById(3);
						int var4 = qs.getQuestVarById(4);

						if (var1 < 1 && targetId == 799513) {
							qs.setQuestVarById(1, 1);
							var1 = qs.getQuestVarById(1);
						} else if (var2 < 1 && targetId == 799514) {
							qs.setQuestVarById(2, 1);
							var2 = qs.getQuestVarById(2);
						} else if (var3 < 1 && targetId == 799515) {
							qs.setQuestVarById(3, 1);
							var3 = qs.getQuestVarById(3);
						} else if (var4 < 1 && targetId == 799516) {
							qs.setQuestVarById(4, 1);
							var4 = qs.getQuestVarById(4);
						}
						updateQuestStatus(env);

						if (var1 * var2 * var3 * var4 == 1) { // All Reians are free
							playQuestMovie(env, 442);
							qs.setQuestVar(4);
							updateQuestStatus(env);
						}
						npc.getController().delete();
						return closeDialogWindow(env);
					}
			}
			return sendQuestSelectionDialog(env);
		} else if (targetId == 799341) { // Anilmo
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 5) {
						return sendQuestDialog(env, 2716);
					}
					break;
				case SETPRO6:
					changeQuestStep(env, 5, 6);// 6
					npc.getController().delete();
					return closeDialogWindow(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}
		int targetId = env.getTargetId();
		int var = qs.getQuestVarById(0);
		if (targetId == 216592) {
			if (var == 4) {
				spawnForFiveMinutes(799341, player.getWorldMapInstance(), 561.8763f, 192.25128f, 135.88919f, (byte) 30);
				qs.setQuestVarById(0, var + 1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 442)
			spawnForFiveMinutes(216592, env.getPlayer().getWorldMapInstance(), (float) 561.8763, (float) 192.25128, (float) 135.88919, (byte) 30);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 20031);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 20031);
	}
}
