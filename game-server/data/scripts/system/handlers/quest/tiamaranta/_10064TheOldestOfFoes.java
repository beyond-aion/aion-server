package quest.tiamaranta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.SystemMessageId;
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
 * @author Luzien
 */
public class _10064TheOldestOfFoes extends QuestHandler {

	private final static int questId = 10064;
	
	private final static List<Integer> mobs = Arrays.asList(800037);
	
	private final static List<Integer> drakans = Arrays.asList(218773, 218775, 218774);

	public _10064TheOldestOfFoes() {
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
		qe.registerOnMovieEndQuest(752, questId);
		qe.registerOnMovieEndQuest(753, questId);
		qe.registerQuestNpc(800018).addOnTalkEvent(questId);
		qe.registerQuestNpc(800021).addOnTalkEvent(questId);
		qe.registerQuestNpc(800022).addOnTalkEvent(questId);
		qe.registerQuestNpc(205886).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env){
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
						TeleportService2.teleportTo(player, 300400000, newInstance.getInstanceId(), 433.27f, 685.31f, 183.4f, (byte) 10, TeleportAnimation.BEAM_ANIMATION);
						changeQuestStep(env, 0, 1, false);
						return closeDialogWindow(env);
					}
				}
			}
			else if (targetId == 800022) {
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							playQuestMovie(env, 752);
							changeQuestStep(env, 1, 2, false);
							return closeDialogWindow(env);
						}
					}
				}
			else if (targetId == 800021) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 3) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO3: 
					case SETPRO4:{
						spawnDrakans(player);
						changeQuestStep(env, 3, 4, false);
						return closeDialogWindow(env);
					}
				}
			}
			}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205886) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
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
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800031,  550.057f, 666.941f,  183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800031,  542.284f, 662.375f,  183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800031,  540.240f, 665.980f,  183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800031,  548.020f, 670.540f,  183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800031,  538.181f, 669.566f,  183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800031,  545.943f, 674.044f,  183.301f, (byte) 40);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800021,  540.155f, 675.154f,  183.301f, (byte) 40);
					// TODO MOVE
					return defaultOnKillEvent(env, env.getTargetId(), 2, 3);
				}
			}
			else if (var == 4 && drakans.contains(env.getTargetId())) {
				if (!getNpcsAlive(player.getPosition().getWorldMapInstance(), drakans)) {
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 218823, 527.996f, 700.251f, 178.393f, (byte) 120);
					return defaultOnKillEvent(env, env.getTargetId(), 4, 5);
				}
			}
			else if (var == 5 && env.getTargetId() == 218823) {
				playQuestMovie(env, 753);
				return defaultOnKillEvent(env, 218823, 5, true);
			}
		}
		return false;
	}
	
	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START){
			int var = qs.getQuestVarById(0);
			if (movieId == 752 && var == 2) {
				spawnLegioners(player);
				return true;
			}
		}
			else if (movieId == 753 && qs != null && qs.getStatus() == QuestStatus.REWARD){
				TeleportService2.teleportTo(player, 600030000, 305.75726f, 1736.2083f, 295.90472f, (byte) 0,  TeleportAnimation.BEAM_ANIMATION);
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
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800022,  514.004f, 718.839f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800033,  504.51f, 728.05f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800032,  500.42f, 735.26f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800033,  496.2f, 742.43f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800032,  500.91f, 727.45f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800033,  495.33f, 727.25f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800033,  491.35f, 734.78f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800034,  511.76f, 732.18f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800033,  507.68f, 739.07f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800032,  503.49f, 746.21f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800034,  514.64f, 735.77f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800034,  511.53f, 741.44f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800033,  516.96f, 739.95f,  178.393f, (byte) 0);
					QuestService.addNewSpawn(300400000, player.getInstanceId(), 800034,  513.035f, 747.17f,  178.393f, (byte) 0);
					return true;
				}
			}
			else {
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
	public boolean onDieEvent(QuestEnv env){
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START){
			int var = qs.getQuestVarById(0);
			if (var >= 1) {
				qs.setQuestVar(0);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1,
					DataManager.QUEST_DATA.getQuestById(questId).getName()));
				return true;
			}
		}
		return false;
	}
	
	private void spawnDrakans(Player player) {
		final List<Npc> drakans = new ArrayList<Npc>();
		drakans.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218773,  550.057f, 666.941f,  183.301f, (byte) 40));
		drakans.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218773,  542.284f, 662.375f,  183.301f, (byte) 40));
		drakans.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218773,  540.240f, 665.980f,  183.301f, (byte) 40));
		drakans.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218775,  548.020f, 670.540f,  183.301f, (byte) 40));
		drakans.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218775,  538.181f, 669.566f,  183.301f, (byte) 40));
		drakans.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218774,  545.943f, 674.044f,  183.301f, (byte) 40));
		drakans.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 218774,  540.155f, 675.154f,  183.301f, (byte) 40));
		for (Npc mob : drakans) {
			mob.setTarget(player);
      mob.getAggroList().addHate(player, 1);
      }
	}
	
	private void spawnLegioners(Player player) {
		final List<Npc> enemylegioners = new ArrayList<Npc>();
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037, 437.11f, 679.46f, 183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037, 434.35f, 676.1f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  431.36f, 672.6f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  434.98f, 683.7f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  431.6f, 681f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  428.7f, 677.26f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  430.1f, 683.9f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  432.53f, 687.9f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  426.15f, 682.9f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  429.917f, 692.7f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  427.48f, 688.82f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  423f, 688f,  183.3f, (byte) 10));
		enemylegioners.add((Npc)QuestService.spawnQuestNpc(300400000, player.getInstanceId(), 800037,  421.8f, 690.4f,  183.3f, (byte) 10));
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
