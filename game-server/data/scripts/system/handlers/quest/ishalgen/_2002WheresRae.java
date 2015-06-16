package quest.ishalgen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
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
import com.aionemu.gameserver.world.WorldMapInstance;


/**
 * @author Mr. Poke
 * @modified Hellboy, Gigi, Bobobear
 */
public class _2002WheresRae extends QuestHandler {

	private final static int questId = 2002;
	private final static int[] npc_ids = { 203519, 203534, 203553, 700045, 203516, 203538 };

	public _2002WheresRae() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(210377).addOnKillEvent(questId);
		qe.registerQuestNpc(210378).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		final QuestEnv qEnv = env;
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203519: {
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
						case SETPRO1:
							if (var == 0) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
				case 203534: {
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
						case SELECT_ACTION_1353:
							playQuestMovie(env, 52);
							break;
						case SETPRO2:
							if (var == 1) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
				case 790002: {
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
							else if (var == 10)
								return sendQuestDialog(env, 2034);
							else if (var == 11)
								return sendQuestDialog(env, 2375);
							else if (var == 12)
								return sendQuestDialog(env, 2462);
							else if (var == 13)
								return sendQuestDialog(env, 2716);
						case SETPRO3:
						case SETPRO4:
						case SETPRO6:
							if (var == 2 || var == 10) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
							else if (var == 13) {
								qs.setQuestVarById(0, 14);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
							break;
						case SETPRO5:
							if (var == 12 || var == 99 ) {
								qs.setQuestVar(99);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								// Create instance
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(320010000);
								InstanceService.registerPlayerWithInstance(newInstance, player);
								TeleportService2.teleportTo(player, 320010000, newInstance.getInstanceId(), 457.65f, 426.8f, 230.4f);
								return true;
							}
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 11) {
								if (QuestService.collectItemCheck(env, true)) {
									qs.setQuestVarById(0, 12);
									updateQuestStatus(env);
									return sendQuestDialog(env, 2461);
								}
								else
									return sendQuestDialog(env, 2376);
							}
					}
				}
					break;
				case 700045:
					if (var == 11 && env.getDialog() == DialogAction.USE_OBJECT) {
						SkillEngine.getInstance().applyEffectDirectly(8343, player, player, 0);
						return true;
					}
				case 203538:
					if (var == 14 && env.getDialog() == DialogAction.USE_OBJECT) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						Npc npc = (Npc) env.getVisibleObject();
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 203553, npc.getX(), npc.getY(),
							npc.getZ(), npc.getHeading());
						npc.getController().onDie(npc);
						//playQuestMovie(env, 256);
						return true;
					}
					break;
				case 203553:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 15)
								return sendQuestDialog(env, 3057);
						case SETPRO7:
							if (var == 15) {
								env.getVisibleObject().getController().onDelete();
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				case 205020:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var >= 12) {
								player.setState(CreatureState.FLIGHT_TELEPORT);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(3001);
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 3001, 0));
								ThreadPoolManager.getInstance().schedule(new Runnable(){
									@Override
									public void run() {
										TeleportService2.teleportTo(player, 220010000, 940.15f, 2295.64f, 265.7f, (byte) 43);
										qs.setQuestVar(13);
										updateQuestStatus(qEnv);
									}
								}, 40000);
								return true;
						}
						return false;
					default:
						return false;
					}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203516) {
				if (env.getDialog() == DialogAction.USE_OBJECT || env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 3398);
				else if (env.getDialog() == DialogAction.SETPRO8)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() != QuestStatus.START)
			return false;
		switch (targetId) {
			case 210377:
			case 210378:
				if (var >= 3 && var < 10) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
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
		return defaultOnLvlUpEvent(env, 2100, true);
	}
}
