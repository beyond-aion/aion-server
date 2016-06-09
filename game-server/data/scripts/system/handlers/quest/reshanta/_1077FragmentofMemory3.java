package quest.reshanta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
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
 * @author Majka
 * @reworked Pad
 */
public class _1077FragmentofMemory3 extends QuestHandler {

	private static final int questId = 1077;
	private static final int azoturanId = 310100000;
	private static final int icaronixNormalId = 233877, icaronixEliteId = 214598;
	private static final int[] npc_ids = { 203704, 798154, 204574, 204652, 204653, 278500 };

	public _1077FragmentofMemory3() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerQuestNpc(icaronixNormalId).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1701, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			return false;
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == npc_ids[0]) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
						break;
					case SETPRO1:
						if (var == 0) {
							changeQuestStep(env, 0, 1, false);
							TeleportService2.teleportTo(player, 210060000, 2278.23f, 2217.8f, 59.27f, (byte)12, TeleportAnimation.FADE_OUT_BEAM);
							return true;
						}
						break;
				}
			} else if (targetId == npc_ids[1]) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
						break;
					case SETPRO2:
						if (var == 1) {
							changeQuestStep(env, 1, 2, false);
							TeleportService2.teleportTo(player, 210040000, 713.6f, 625.36f, 129.75f, (byte)0, TeleportAnimation.FADE_OUT_BEAM);
							return true;
						}
						break;
				}
			} else if (targetId == npc_ids[2]) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 2) {
							return sendQuestDialog(env, 1693);
						}
						break;
					case SETPRO3:
						changeQuestStep(env, 2, 3, false);
						WorldMapInstance instance = InstanceService.getNextAvailableInstance(azoturanId);
						InstanceService.registerPlayerWithInstance(instance, player);
						Npc icaronixElite = instance.getNpc(icaronixEliteId);
						if (icaronixElite != null) {
							icaronixElite.getController().delete();
							QuestService.spawnQuestNpc(azoturanId, instance.getInstanceId(), icaronixNormalId, 461.07f, 439.876f, 993.046f, (byte) 58);
						}
						TeleportService2.teleportTo(player, azoturanId, instance.getInstanceId(), 305.8f, 334.46f, 1019.69f, (byte) 27, TeleportAnimation.FADE_OUT_BEAM);
						return true;
				}
			} else if (targetId == npc_ids[3]) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 6) {
							return sendQuestDialog(env, 3057);
						} else if (var >= 3) {
							return sendQuestDialog(env, 2034);
						}
						break;
					case SETPRO10:
						if (var == 3) {
							changeQuestStep(env, 3, 4, false);
						}
						closeDialogWindow(env);
						player.setState(CreatureState.FLIGHT_TELEPORT);
						player.unsetState(CreatureState.ACTIVE);
						player.setFlightTeleportId(71001);
						PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 71001, 0));
						return true;
					case SET_SUCCEED:
						if (var == 6) {
							if (defaultCloseDialog(env, 6, 6, true, false)) {
								TeleportService2.teleportToNpc(player, npc_ids[5]);
								return true;
							}
						}
						break;
				}
			} else if (targetId == npc_ids[4]) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var >= 4) {
							return sendQuestDialog(env, 2375);
						}
						break;
					case SELECT_ACTION_2376:
						if (var == 4) {
							playQuestMovie(env, 421);
						}
						break;
					case SETPRO11:
						if (var == 4) {
							changeQuestStep(env, 4, 5, false);
						}
						closeDialogWindow(env);
						player.setState(CreatureState.FLIGHT_TELEPORT);
						player.unsetState(CreatureState.ACTIVE);
						player.setFlightTeleportId(72001);
						PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 72001, 0));
						return true;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npc_ids[5]) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (dialog == DialogAction.SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		if (defaultOnKillEvent(env, icaronixNormalId, 5, 6)) {
			playQuestMovie(env, 422);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() != azoturanId) {
				int var = qs.getQuestVarById(0);
				if (var >= 3 && var <= 6) {
					qs.setQuestVarById(0, 2);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

}
