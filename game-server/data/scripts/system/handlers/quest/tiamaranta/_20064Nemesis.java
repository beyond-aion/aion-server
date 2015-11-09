package quest.tiamaranta;

import java.util.Arrays;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
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
public class _20064Nemesis extends QuestHandler {

	private final static int questId = 20064;

	private final static List<Integer> mobs = Arrays.asList(800033);

	private final static List<Integer> drakans = Arrays.asList(218773, 218775, 218774);

	public _20064Nemesis() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnDie(questId);
		qe.registerOnEnterWorld(questId);
		for (int mob : mobs)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		for (int mob : drakans)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		qe.registerQuestNpc(218823).addOnKillEvent(questId);
		qe.registerOnMovieEndQuest(755, questId);
		qe.registerOnMovieEndQuest(756, questId);
		qe.registerQuestNpc(800018).addOnTalkEvent(questId);
		qe.registerQuestNpc(800021).addOnTalkEvent(questId);
		qe.registerQuestNpc(800035).addOnTalkEvent(questId);
		qe.registerQuestNpc(205886).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 800018) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
					}
					case SETPRO1: {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300400000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 300400000, newInstance.getInstanceId(), 433.27f, 685.31f, 183.4f, (byte) 10,
							TeleportAnimation.FADE_OUT_BEAM);
						changeQuestStep(env, 0, 1, false);
						return closeDialogWindow(env);
					}
				}
			} else if (targetId == 800035) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO2: {
						playQuestMovie(env, 755);
						changeQuestStep(env, 1, 2, false);
						return closeDialogWindow(env);
					}
				}
			} else if (targetId == 800021) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 3) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO3:
					case SETPRO4: {
						spawnDrakans(player);
						changeQuestStep(env, 3, 4, false);
						return closeDialogWindow(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205886) {
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
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2 && mobs.contains(env.getTargetId())) {
				if (!getNpcsAlive(player.getPosition().getWorldMapInstance(), mobs)) {
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800041, 550.057f, 666.941f, 183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800041, 542.284f, 662.375f, 183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800041, 540.240f, 665.980f, 183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800041, 548.020f, 670.540f, 183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800041, 538.181f, 669.566f, 183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800041, 545.943f, 674.044f, 183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800021, 540.155f, 675.154f, 183.301f, (byte) 40);
					// TODO MOVE
					return defaultOnKillEvent(env, env.getTargetId(), 2, 3);
				}
			} else if (var == 4 && drakans.contains(env.getTargetId())) {
				if (!getNpcsAlive(player.getPosition().getWorldMapInstance(), drakans)) {
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 218823, 527.996f, 700.251f, 178.393f, (byte) 120);
					return defaultOnKillEvent(env, env.getTargetId(), 4, 5);
				}
			} else if (var == 5 && env.getTargetId() == 218823) {
				playQuestMovie(env, 756);
				return defaultOnKillEvent(env, 218823, 5, true);
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (movieId == 755 && var == 2) {
				spawnLegioners(player);
				return true;
			}
		} else if (movieId == 756 && qs != null && qs.getStatus() == QuestStatus.REWARD) {
			TeleportService2.teleportTo(player, 600030000, 305.75726f, 1736.2083f, 295.90472f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			return true;
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (player.getWorldId() == 300400000) {
				if (var == 1) {
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800035, 514.004f, 718.839f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800036, 504.51f, 728.05f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800037, 500.42f, 735.26f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800038, 496.2f, 742.43f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800036, 500.91f, 727.45f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800036, 495.33f, 727.25f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800037, 491.35f, 734.78f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800038, 511.76f, 732.18f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800037, 507.68f, 739.07f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800038, 503.49f, 746.21f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800036, 514.64f, 735.77f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800036, 511.53f, 741.44f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800036, 516.96f, 739.95f, 178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800036, 513.035f, 747.17f, 178.393f, (byte) 0);
					return true;
				}
			} else {
				if (var >= 1) {
					qs.setQuestVarById(0, 0);
					updateQuestStatus(env);
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
			if (var >= 1) {
				qs.setQuestVar(0);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_SYSTEMMSG_GIVEUP(DataManager.QUEST_DATA.getQuestById(questId)
					.getName()));
				return true;
			}
		}
		return false;
	}

	private void spawnDrakans(Player player) {
		final List<Npc> drakans = new FastTable<Npc>();
		drakans.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218773, 550.057f, 666.941f, 183.301f, (byte) 40));
		drakans.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218773, 542.284f, 662.375f, 183.301f, (byte) 40));
		drakans.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218773, 540.240f, 665.980f, 183.301f, (byte) 40));
		drakans.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218775, 548.020f, 670.540f, 183.301f, (byte) 40));
		drakans.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218775, 538.181f, 669.566f, 183.301f, (byte) 40));
		drakans.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218774, 545.943f, 674.044f, 183.301f, (byte) 40));
		drakans.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218774, 540.155f, 675.154f, 183.301f, (byte) 40));
		for (Npc mob : drakans) {
			mob.setTarget(player);
			mob.getAggroList().addHate(player, 1);
		}
	}

	private void spawnLegioners(Player player) {
		final List<Npc> enemylegioners = new FastTable<Npc>();
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 437.11f, 679.46f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 434.35f, 676.1f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 431.36f, 672.6f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 434.98f, 683.7f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 431.6f, 681f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 428.7f, 677.26f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 430.1f, 683.9f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 432.53f, 687.9f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 426.15f, 682.9f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 429.917f, 692.7f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 427.48f, 688.82f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 423f, 688f, 183.3f, (byte) 10));
		enemylegioners.add((Npc) QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800033, 421.8f, 690.4f, 183.3f, (byte) 10));
		for (Npc mob : enemylegioners) {
			mob.getSpawn().setX(504.99f);
			mob.getSpawn().setY(737);
			mob.getSpawn().setZ(178);
			((AbstractAI) mob.getAi2()).setStateIfNot(AIState.WALKING);
			mob.setState(1);
			mob.getMoveController().moveToPoint(504.99f, 737, 178);
			PacketSendUtility.broadcastPacket(mob, new SM_EMOTION(mob, EmotionType.START_EMOTE2, 0, mob.getObjectId()));
		}
	}

	private boolean getNpcsAlive(WorldMapInstance instance, List<Integer> list) {
		for (Integer i : list) {
			for (Npc npc : instance.getNpcs(i)) {
				if (!NpcActions.isAlreadyDead(npc))
					return true;
			}
		}
		return false;
	}
}
