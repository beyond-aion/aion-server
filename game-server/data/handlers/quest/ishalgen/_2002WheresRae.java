package quest.ishalgen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Mr. Poke, Hellboy, Gigi, Bobobear, Majka
 */
public class _2002WheresRae extends AbstractQuestHandler {

	public _2002WheresRae() {
		super(2002);
	}

	@Override
	public void register() {
		int[] npc_ids = { 203519, 203534, 203553, 700045, 203516, 203538, 790002 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(210377).addOnKillEvent(questId);
		qe.registerQuestNpc(210378).addOnKillEvent(questId);
		qe.registerOnFlightPathEnd(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203519:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					return false;
				case 203534:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SELECT2_1:
							playQuestMovie(env, 52);
							break;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					return false;
				case 790002:
					switch (dialogActionId) {
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
							return false;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3);
						case SETPRO4:
							return defaultCloseDialog(env, 10, 11);
						case SETPRO6:
							return defaultCloseDialog(env, 13, 14);
						case SETPRO5:
							if (var == 12) {
								closeDialogWindow(env);
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.ATAXIAR.getId(), player);
								TeleportService.teleportTo(player, newInstance, 457.65f, 426.8f, 230.4f);
								return true;
							}
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 11) {
								if (QuestService.collectItemCheck(env, true)) {
									changeQuestStep(env, 11, 12);
									return sendQuestDialog(env, 2461);
								} else
									return sendQuestDialog(env, 2376);
							}
					}
					break;
				case 700045:
					if (var == 11 && env.getDialogActionId() == USE_OBJECT) {
						SkillEngine.getInstance().applyEffectDirectly(8343, player, player);
						return true;
					}
					return false;
				case 203538:
					if (env.getDialogActionId() == USE_OBJECT && changeQuestStep(env, 14, 15)) {
						Npc npc = (Npc) env.getVisibleObject();
						spawnForFiveMinutes(203553, npc.getPosition());
						ThreadPoolManager.getInstance().schedule(() -> {
							if (qs.getStatus() == QuestStatus.START) // reset the quest step if Rae despawned without having been talked to
								changeQuestStep(env, 15, 14);
						}, 60000 * 5 + 1000);
						npc.getController().deleteAndScheduleRespawn();
						return closeDialogWindow(env);
					}
					break;
				case 203553:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 15)
								return sendQuestDialog(env, 3057);
							return false;
						case SETPRO7:
							if (changeQuestStep(env, 15, 15, true)) {
								env.getVisibleObject().getController().delete();
								return closeDialogWindow(env);
							}
					}
					return false;
				case 205020:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (qs.getQuestVars().getQuestVars() == 99) {
								player.setState(CreatureState.FLYING);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(4001);
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 4001, 0));
								return true;
							}
							return false;
						default:
							return false;
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203516) {
				if (dialogActionId == USE_OBJECT || dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 3398);
				else if (dialogActionId == SETPRO8)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		if (player.getWorldId() == WorldMapType.ATAXIAR.getId())
			return changeQuestStep(env, 12, 99);
		return changeQuestStep(env, 99, 12);
	}

	@Override
	public boolean onFlightPathEndEvent(QuestEnv env, int teleportId) {
		if (teleportId != 4001 || !changeQuestStep(env, 99, 13))
			return false;
		TeleportService.teleportTo(env.getPlayer(), 220010000, 940.15f, 2295.64f, 265.7f, (byte) 43);
		return true;
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
					changeQuestStep(env, var, Math.min(10, var + 1));
					return true;
				}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 2100);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 2100);
	}
}
