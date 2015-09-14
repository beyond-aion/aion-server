package quest.tiamaranta;

import java.util.Collections;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;


/**
 * @author Cheatkiller
 *
 */
public class _41552EnosAvenger extends QuestHandler {

	private final static int questId = 41552;
	
	public _41552EnosAvenger() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestSkill(10377, questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerQuestNpc(205968).addOnQuestStart(questId);
		qe.registerQuestNpc(205968).addOnTalkEvent(questId);
		qe.registerQuestNpc(701144).addOnTalkEvent(questId);
		qe.registerQuestNpc(218781).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205968) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182212546, 1);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			Npc npc = (Npc) env.getVisibleObject();
			if (targetId == 205968) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
					if (player.getInventory().getItemCountByItemId(182212547) >= 1) {
						sendMsg(1111515, npc.getObjectId(), true, 1000);
					  QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 218781, 2790.97f, 1015.23f, 159.87f, (byte) 0);
					}
					return checkQuestItems(env, 0, 1, false, 0, 10001);
				}
			}
			else if(targetId == 701144) {
				ItemService.addQuestItems(player, Collections.singletonList(new QuestItems(182212586, 1)));
				npc.getController().onDelete();
				return true;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			int itemCount = (int) player.getInventory().getItemCountByItemId(182212586);
			int choise = 0;
			if (itemCount > 0 && itemCount < 2)
				choise = 0;
			else if (itemCount > 2 && itemCount < 4)
				choise = 1;
			else if (itemCount >= 4)
				choise = 2;
			if (targetId == 205968) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						player.getInventory().decreaseByItemId(182212586, itemCount);
						return sendQuestEndDialog(env, choise);
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				qs.setStatus(QuestStatus.REWARD);
				qs.setQuestVar(2);
				updateQuestStatus(env);
				despawnCristal(env);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD)) {
			  Npc npc = (Npc) env.getVisibleObject();
			  QuestService.questTimerStart(env, 10);
			  rndSpawn(npc, 701144, 8);
		    return defaultOnKillEvent(env, 218781, 1, 2);
		}
		return false;
	}
	
	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD)) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				qs.setStatus(QuestStatus.REWARD);
				qs.setQuestVar(2);
				updateQuestStatus(env);
				despawnCristal(env);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onUseSkillEvent(QuestEnv env, int skillUsedId) {
		Player player = env.getPlayer();
		Npc npc = (Npc) player.getTarget();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && skillUsedId == 10377) {
			if (npc == null && player.getInventory().getItemCountByItemId(182212547) >= 1)
				return false;
			if(player.getTarget() == npc && npc.getNpcId() == 218497) {
				removeQuestItem(env, 182212546, 1);
				giveQuestItem(env, 182212547, 1);
				npc.getController().onDelete();
				return true;
			}
		}
		return false;
	}
	
	private void despawnCristal(QuestEnv env) {
		Player player = env.getPlayer();
		WorldMapInstance instance = player.getPosition().getWorldMapInstance();
		for (Npc npc: instance.getNpcs()) {
			if (npc.getNpcId() == 701144) {
				npc.getController().onDelete();
			}
		}
	}
	
	private void rndSpawn(Npc npc, int npcId, int count) {
		for (int i = 0; i < count; i++) {
			SpawnTemplate template = rndSpawnInRange(npc, npcId);
			SpawnEngine.spawnObject(template, npc.getInstanceId());
		}
	}
	
	private SpawnTemplate rndSpawnInRange(Npc npc, int npcId) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * 10);
		float y1 = (float) (Math.sin(Math.PI * direction) * 10);
		return SpawnEngine.addNewSingleTimeSpawn(npc.getWorldId(), npcId, npc.getX() + x1, npc.getY()
				+ y1, npc.getZ(), npc.getHeading());
	}
	
	private void sendMsg(final int msg, final int Obj, final boolean isShout, int time) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						if (player.isOnline()) {
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(isShout, msg, Obj, 0));
						}
					}
				});
			}

		}, time);
	}

}