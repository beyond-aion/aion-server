package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
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
 * @author Mr. Poke, Rolandas, vlog
 */
public class _2900NoEscapingDestiny extends AbstractQuestHandler {

	public _2900NoEscapingDestiny() {
		super(2900);
	}

	@Override
	public void register() {
		int[] npcs = { 204182, 203550, 790003, 790002, 203546, 204264, 204061 };
		int[] stigmas = { 140000001, 140000002, 140000003, 140000004 };
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(204263).addOnKillEvent(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		for (int stigma : stigmas) {
			qe.registerOnEquipItem(stigma, questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVars().getQuestVars();
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204182: // Heimdall
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							if (defaultCloseDialog(env, 0, 1)) { // 1
								TeleportService.teleportTo(player, 220010000, 1, 389.0f, 1896.0f, 327.5f, (byte) 61, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
					}
					break;
				case 203550: // Munin
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 10) {
								return sendQuestDialog(env, 4080);
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2); // 2
						case SETPRO10:
							defaultCloseDialog(env, 10, 10, true, false); // reward
							TeleportService.teleportTo(player, 120010000, 1294.8f, 1213.8f, 214.34f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
							return true;
					}
					break;
				case 790003: // Urd
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3); // 3
					}
					break;
				case 790002: // Verdandi
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO4:
							return defaultCloseDialog(env, 3, 4); // 4
					}
					break;
				case 203546: // Skuld
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							} else if (var == 9) {
								return sendQuestDialog(env, 3739);
							}
							return false;
						case SETPRO5:
							if (var == 4) {
								changeQuestStep(env, 4, 95); // 95
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.SPACE_OF_DESTINY.getId(), player);
								TeleportService.teleportTo(player, newInstance, 270.8424f, 249.1182f, 125.8369f, (byte) 60, TeleportAnimation.FADE_OUT_BEAM);
								return closeDialogWindow(env);
							}
							return false;
						case SETPRO9:
							changeQuestStep(env, 9, 10); // 10
							TeleportService.teleportTo(player, 220010000, 1, 383.0f, 1896.0f, 327.625f, (byte) 60, TeleportAnimation.FADE_OUT_BEAM);
							return closeDialogWindow(env);
					}
					break;
				case 204264: // Skuld 2
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 95) {
								return sendQuestDialog(env, 2716);
							} else if (var == 96 || var == 99) {
								return sendQuestDialog(env, 3057);
							} else if (var == 97) {
								return sendQuestDialog(env, 3398);
							}
							return false;
						case SETPRO6:
							if (var == 95) {
								playQuestMovie(env, 156);
								return closeDialogWindow(env);
							}
							return false;
						case SELECT7_1:
							if (giveQuestItem(env, getStoneId(player), 1))
								changeQuestStep(env, 96, 99); // 99
							return sendQuestDialog(env, 3058);
						case SETPRO7:
							if (var == 99) {
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), DialogPage.STIGMA.id()));
								return true;
							}
							return false;
						case SETPRO8:
							if (var == 97) {
								changeQuestStep(env, 97, 98); // 98
								spawnForFiveMinutes(204263, player.getWorldMapInstance(), 257.5f, 245f, 125f, (byte) 0);
								return closeDialogWindow(env);
							}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204061) { // Aud
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 156)
			changeQuestStep(env, 95, 96);
	}

	@Override
	public boolean onEquipItemEvent(QuestEnv env, int itemId) {
		changeQuestStep(env, 99, 97); // 97
		return closeDialogWindow(env);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 98) {
				changeQuestStep(env, 98, 9); // 9
				TeleportService.teleportTo(player, 220010000, 1, 1112.492f, 1718.974f, 270.45917f, (byte) 113, TeleportAnimation.FADE_OUT_BEAM);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var >= 95 && var <= 99) {
				removeStigma(env);
				changeQuestStep(env, var, 4);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (player.getWorldId() != 320070000) {
				if (var >= 95 && var <= 99) {
					removeStigma(env);
					changeQuestStep(env, var, 4);
					return true;
				} else if (var == 9) {
					removeStigma(env);
					return true;
				}
			}
		}
		return false;
	}

	private int getStoneId(Player player) {
		// TODO: find out the correct stigma ids for each class on official servers
		switch (player.getPlayerClass()) {
			case CHANTER:
			case CLERIC:
			case BARD:
				return 140000001; // Healight Light II
			case RIDER:
			case GUNNER:
			case RANGER:
				return 140000002; // Flame Cage I
			case GLADIATOR:
			case ASSASSIN:
			case TEMPLAR:
				return 140000003; // Ferocious Strike III (melee weapon required)
			case SORCERER:
			case SPIRIT_MASTER:
				return 140000004; // Hydro Eruption II
			default:
				throw new UnsupportedOperationException("Unhandled player class " + player.getPlayerClass());
		}
	}

	private void removeStigma(QuestEnv env) {
		Player player = env.getPlayer();
		int stigmaId = getStoneId(player);
		for (Item item : player.getEquipment().getEquippedItemsByItemId(stigmaId)) {
			player.getEquipment().unEquipItem(item.getObjectId());
		}
		removeQuestItem(env, stigmaId, 1);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
