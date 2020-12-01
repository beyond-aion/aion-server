package quest.clash_of_destiny;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Enomine
 */
public class _24030ShowdownWithDestiny extends AbstractQuestHandler {

	private final static int[] mobs = { 214591, 798346, 798344, 798342, 798345, 798343 };

	public _24030ShowdownWithDestiny() {
		super(24030);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204206, 204207, 203550, 700551, 205020, 204052 };
		qe.registerOnEnterWorld(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc : mobs)
			qe.registerQuestNpc(npc).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204206:// Cavalorn
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 204207:// Kasir
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO2:
							TeleportService.teleportTo(player, WorldMapType.ISHALGEN.getId(), 386f, 1895.4f, 327.62f, (byte) 60, TeleportAnimation.FADE_OUT_BEAM);
							return defaultCloseDialog(env, 1, 2);
					}
					break;
				case 203550:// Munin
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
							if (var == 3)
								return sendQuestDialog(env, 2034);
							if (var == 4)
								return sendQuestDialog(env, 2375);
							if (var == 8)
								return sendQuestDialog(env, 3739);
							return false;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3);
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 3 && player.getInventory().getItemCountByItemId(182215391) == 1) {
								removeQuestItem(env, 182215391, 1);
								changeQuestStep(env, var, 4);
								return sendQuestDialog(env, 10000);
							}
							return sendQuestDialog(env, 10001);
						case SETPRO5:
							giveQuestItem(env, workItems.get(0).getItemId(), workItems.get(0).getCount());
							giveQuestItem(env, workItems.get(1).getItemId(), workItems.get(1).getCount());
							TeleportService.teleportTo(player, WorldMapType.RESHANTA.getId(), 2241f, 2191.5f, 2190.1f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
							return defaultCloseDialog(env, 4, 5);
						case SET_SUCCEED:
							if (var == 8) {
								changeQuestStep(env, var, var, true);
								return sendQuestSelectionDialog(env);
							}
					}
					break;
				case 700551: // Fissure of Destiny
					if (dialogActionId == USE_OBJECT && var == 5) {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.IDAB_PRO_D3.getId(), player);
						TeleportService.teleportTo(player, newInstance, 52, 174, 229);
						return true;
					}
					break;
				case 205020:// Hagen
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2716);
						case SETPRO6:
							SkillEngine.getInstance().applyEffectDirectly(18567, player, player);
							SkillEngine.getInstance().applyEffectDirectly(18568, player, player);
							SkillEngine.getInstance().applyEffectDirectly(18569, player, player);
							SkillEngine.getInstance().applyEffectDirectly(18570, player, player);
							player.setState(CreatureState.FLYING);
							player.unsetState(CreatureState.ACTIVE);
							player.setFlightTeleportId(1001);
							PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 1001, 0));
							return defaultCloseDialog(env, 5, 6);
					}
					break;
			}
		}
		if (targetId == 204052 && qs.getStatus() == QuestStatus.REWARD) {// Vidar
			switch (dialogActionId) {
				case USE_OBJECT:
					return sendQuestDialog(env, 10002);
				default: {
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
			int var1 = qs.getQuestVarById(1);
			if (var == 6) {
				if (var1 != 49)
					return defaultOnKillEvent(env, mobs, 0, 49, 1);
				else if (var1 == 49) {
					changeQuestStep(env, var, 7);
					Npc mob = (Npc) spawnInFrontOf(798346, player);
					mob.getAggroList().addHate(player, 100);
					return true;
				}
			}
			if (env.getTargetId() == 798346) {
				qs.setQuestVar(8);
				updateQuestStatus(env);
				TeleportService.teleportTo(player, 220010000, (float) 385, (float) 1895, (float) 327, (byte) 58);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		if (player.getWorldId() != WorldMapType.IDAB_PRO_D3.getId()) {
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 6 || var == 7) {
					qs.setQuestVar(5);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
