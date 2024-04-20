package quest.ascension;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ASCENSION_MORPH;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ClassChangeService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.reward.WebRewardService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Talk with Pernos (790001). Go to the island at the center of Cliona Lake (CLIONA_LAKE_210010000) and fill up the bottle Pernos gave you
 * (182200007). Meet Daminu (730008) and obtain Daminu's Essence (182200009). Talk with Pernos. Explore your lost past (310020000, 52, 174, 229).
 * Advance on Karamatis (Belpartan, 205000). Defeat Raiders (211042) (4). Defeat Orissan (211043). Talk with Pernos and choose the path you will take.
 * 
 * @author MrPoke, vlog
 */
public class _1006Ascension extends AbstractQuestHandler {

	public _1006Ascension() {
		super(1006);
	}

	@Override
	public void register() {
		if (CustomConfig.ENABLE_SIMPLE_2NDCLASS)
			return;
		int[] mobs = { 211042, 211043 };
		int[] npcs = { 790001, 730008, 205000 };
		qe.registerOnLevelChanged(questId);
		qe.registerOnQuestCompleted(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182200007, questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int dialogActionId = env.getDialogActionId();
		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 790001: // Pernos
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 3)
								return sendQuestDialog(env, 1693);
							else if (var == 5)
								return sendQuestDialog(env, 2034);
							return false;
						case SETPRO1:
							if (player.getInventory().getItemCountByItemId(182200007) == 0)
								giveQuestItem(env, 182200007, 1);
							qs.setQuestVar(1);
							updateQuestStatus(env);
							TeleportService.teleportTo(player, 210010000, 657f, 1071f, 99.375f, (byte) 72, TeleportAnimation.FADE_OUT_BEAM);
							return true;
						case SETPRO3:
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.KARAMATIS_B.getId(), player);
							TeleportService.teleportTo(player, newInstance, 52, 174, 229, (byte) 10, TeleportAnimation.NONE);
							qs.setQuestVar(99); // 99
							updateQuestStatus(env);
							removeQuestItem(env, 182200009, 1);
							return closeDialogWindow(env);
						case SETPRO4:
							int dialogPageId = ClassChangeService.getClassSelectionDialogPageId(player.getRace(), player.getPlayerClass());
							if (var == 5 && dialogPageId != 0)
								return sendQuestDialog(env, dialogPageId);
							return false;
						case SETPRO5:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.GLADIATOR);
						case SETPRO6:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.TEMPLAR);
						case SETPRO7:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.ASSASSIN);
						case SETPRO8:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.RANGER);
						case SETPRO9:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.SORCERER);
						case SETPRO10:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.SPIRIT_MASTER);
						case SETPRO11:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.CLERIC);
						case SETPRO12:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.CHANTER);
						case SETPRO13:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.GUNNER);
						case SETPRO14:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.BARD);
						case SETPRO15:
							return var == 5 && setPlayerClass(env, qs, PlayerClass.RIDER);
					}
					break;
				case 730008: // Daminu
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2 && player.getInventory().getItemCountByItemId(182200008) >= 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SELECT2_1:
							playQuestMovie(env, 14);
							return sendQuestDialog(env, 1353);
						case SETPRO2:
							if (var == 2) {
								removeQuestItem(env, 182200008, 1);
								giveQuestItem(env, 182200009, 1);
								qs.setQuestVar(3);
								updateQuestStatus(env);
								TeleportService.teleportTo(player, 210010000, 246f, 1639f, 100.316f, (byte) 56, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
					}
					break;
				case 205000: // Belpartan
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (qs.getQuestVars().getQuestVars() == 99) {
								SkillEngine.getInstance().applyEffectDirectly(281, player, player);
								player.setState(CreatureState.FLYING);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(1001);
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 1001, 0));
								qs.setQuestVar(50); // 50
								updateQuestStatus(env);
								ThreadPoolManager.getInstance().schedule(() -> {
									qs.setQuestVar(51);
									updateQuestStatus(env);
									List<Npc> mobs = new ArrayList<>();
									mobs.add((Npc) spawn(211042, player, 224.073f, 239.1f, 206.7f, (byte) 0));
									mobs.add((Npc) spawn(211042, player, 233.5f, 241.04f, 206.365f, (byte) 0));
									mobs.add((Npc) spawn(211042, player, 229.6f, 265.7f, 205.7f, (byte) 0));
									mobs.add((Npc) spawn(211042, player, 222.8f, 262.5f, 205.7f, (byte) 0));
									for (Npc mob : mobs) {
										mob.getAggroList().addHate(player, 1000);
									}
								}, 43000);
								return true;
							}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790001) { // Pernos
				switch (env.getDialogActionId()) {
					case SELECTED_QUEST_NOREWARD:
						if (player.getWorldId() == 310020000)
							TeleportService.teleportTo(player, 210010000, 245.14868f, 1639.1372f, 100.35713f, (byte) 60, TeleportAnimation.FADE_OUT_BEAM);
						break;
				}
				return sendQuestEndDialog(env); // finishes quest or shows reward selection
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideItemUseZone(ZoneName.get("LF1_ITEMUSEAREA_Q1006"))) {
				int var = qs.getQuestVarById(0);
				if (var == 1) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 1, 2, false, 182200008, 1, 0)); // 2
				}
			}
		}
		return HandlerResult.SUCCESS; // ??
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			int targetId = env.getTargetId();
			if (targetId == 211042) {
				env.getVisibleObject().getController().delete();
				if (var >= 51 && var < 54) {
					return defaultOnKillEvent(env, 211042, 51, 54); // 52 - 54
				} else if (var == 54) {
					qs.setQuestVar(4); // 4
					updateQuestStatus(env);
					Npc mob = (Npc) spawn(211043, player, 226.7f, 251.5f, 205.5f, (byte) 0);
					mob.getAggroList().addHate(player, 1000);
					return true;
				}
			} else if (targetId == 211043 && var == 4) {
				playQuestMovie(env, 151);
				player.getWorldMapInstance().forEachNpc(npc -> npc.getController().delete());
				spawn(790001, player, 220.6f, 247.8f, 206.0f, (byte) 0);
				qs.setQuestVar(5); // 5
				updateQuestStatus(env);
			}
		}
		return false;
	}

	private boolean setPlayerClass(QuestEnv env, QuestState qs, PlayerClass playerClass) {
		if (ClassChangeService.setClass(env.getPlayer(), playerClass)) {
			changeQuestStep(env, 5, 5, true); // reward
			return sendQuestDialog(env, 5);
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && player.getWorldId() == 310020000) {
			int var = qs.getQuestVars().getQuestVars();
			if (var > 3)
				changeQuestStep(env, var, 3);
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (player.getWorldId() == 310020000)
				PacketSendUtility.sendPacket(player, new SM_ASCENSION_MORPH(1));
			else if (var > 3 && var != 5) // 5 is class selection, quest should not reset anymore after you killed orissan
				changeQuestStep(env, var, 3);
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		if (env.getQuestId() == questId) {
			Player player = env.getPlayer();
			player.getCommonData().updateDaeva();
			if (WebRewardService.MaxLevelReward.isPendingAscension(player))
				WebRewardService.MaxLevelReward.reward(player);
		}
	}
}
