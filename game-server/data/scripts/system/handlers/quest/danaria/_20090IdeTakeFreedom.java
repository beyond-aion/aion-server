package quest.danaria;

import java.util.Arrays;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Cheatkiller
 */
public class _20090IdeTakeFreedom extends QuestHandler {

	private final static int questId = 20090;
	
	private final static List<Integer> mobs = Arrays.asList(231069, 231067, 231068);

	public _20090IdeTakeFreedom() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 800567, 800821, 730736, 800822, 800823, 730735, 800818, 800825 };
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnMovieEndQuest(852, questId);
		qe.registerQuestNpc(701545).addOnKillEvent(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnEnterZone(ZoneName.get("IDGEL_STORAGE_1_301000000"), questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20085);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 800567) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 0) {
							return sendQuestDialog(env, 1011);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			}
			else if (targetId == 800821) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO2: {
						return defaultCloseDialog(env, 1, 2); 
					}
				}
			}
			else if (targetId == 730736) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 2) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO3: {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(301000000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 301000000, newInstance.getInstanceId(), 211.75f, 510.34f, 153.22f, (byte) 0, TeleportAnimation.BEAM_ANIMATION);
						return defaultCloseDialog(env, 2, 3, 182215248, 1); 
					}
				}
			}
			else if (targetId == 800822) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 3) {
							return sendQuestDialog(env, 2034);
						}
					}
					case SETPRO4: {
						return defaultCloseDialog(env, 3, 4); 
					}
				}
			}
			else if (targetId == 730735) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 6) {
							return sendQuestDialog(env, 3057);
						}
					}
					case SETPRO7: {
						Npc npc = (Npc) env.getVisibleObject();
						removeQuestItem(env, 182215248, 1);
						QuestService.questTimerStart(env, 120);
						QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 701547, npc.getX(), npc.getY(), npc.getZ(), (byte) 42);
						npc.getController().onDelete();
						spawnAttackers(player);
						return defaultCloseDialog(env, 6, 7); 
					}
				}
			}
			else if (targetId == 800823) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 8) {
							return sendQuestDialog(env, 3398);
						}
					}
					case SET_SUCCEED: {
						TeleportService2.teleportTo(player, 600060000, 1, 158.7789f, 944.99554f, 547.9603f, (byte) 88, TeleportAnimation.BEAM_ANIMATION);
						return defaultCloseDialog(env, 8, 8, true, false); 
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800825) {
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
	
	private void spawnAttackers(final Player player) {
		final int mobToSpawn = mobs.get(Rnd.get(0, 2));
		Npc spawn = (Npc) QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), mobToSpawn, 510.5f, 485.28f, 97.38f, (byte) 100);
		SkillEngine.getInstance().applyEffectDirectly(20700, spawn, spawn, 0);
		((AbstractAI) spawn.getAi2()).setStateIfNot(AIState.WALKING);
		spawn.setState(1);
		spawn.getMoveController().moveToPoint(542, 435, 94);
		PacketSendUtility.broadcastPacket(spawn, new SM_EMOTION(spawn, EmotionType.START_EMOTE2, 0, spawn.getObjectId()));
	}
	
	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 3) {
				if (player.getWorldId() == 301000000) {
					playQuestMovie(env, 852);
					return true;
				}
			}
			else if (var > 3) {
				if (player.getWorldId() != 301000000) {
					changeQuestStep(env, var, 2, false);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			final int var = qs.getQuestVarById(0);
			if (var == 4) {
				return defaultOnKillEvent(env, 701545, 4, 5);
			}
			else if (var == 7) {
				int targetId = env.getTargetId();
				if (mobs.contains(targetId)) {
					ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							if (var == 8) {
								return;
							}
							spawnAttackers(player);
						}
					}, 10000);
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
		boolean isDeadGen = true;
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			for (Npc npc : World.getInstance().getNpcs()) {
				if (npc.getNpcId() == 701547) {
					isDeadGen = false;
				}
			}
			if (var == 7) {
				if (!isDeadGen) {
					changeQuestStep(env, 7, 8, false);
					return true;
				}
				else {
					changeQuestStep(env, 7, 2, false);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 5) {
			changeQuestStep(env, 5, 6, false);
			QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 800823, 553.97f, 430.22f, 94.8f, (byte) 45);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 800822, 218.56f, 510, 153.22f, (byte) 66);
			return true;
		}
		return false;
	}
}
