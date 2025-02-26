package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
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
public class _1929ASliverofDarkness extends AbstractQuestHandler {

	public _1929ASliverofDarkness() {
		super(1929);
	}

	@Override
	public void register() {
		int[] npcs = { 203752, 203852, 203164, 205110, 700240, 205111, 203701, 203711 };
		int[] stigmas = { 140000001, 140000002, 140000003, 140000004 };
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(212992).addOnKillEvent(questId);
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
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		int var = qs.getQuestVars().getQuestVars();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203752: // Jucleas
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 203852: // Ludina
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO2:
							if (defaultCloseDialog(env, 1, 2)) { // 2
								TeleportService.teleportToNpc(player, 203164);
								return true;
							}
					}
					break;
				case 203164: // Morai
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							} else if (var == 8) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case SETPRO3:
							if (var == 2) {
								changeQuestStep(env, 2, 93); // 93
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.IDLF1B_STIGMA.getId(), player);
								TeleportService.teleportTo(player, newInstance, 338, 101, 1191);
								return closeDialogWindow(env);
							}
							return false;
						case SETPRO7:
							if (defaultCloseDialog(env, 8, 9)) { // 9
								TeleportService.teleportToNpc(player, 203701);
								return true;
							}
					}
					break;
				case 205110: // Icaronix
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 93) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO4:
							if (var == 93) {
								changeQuestStep(env, 93, 94); // 94
								player.setState(CreatureState.FLYING);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(31001);
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 31001, 0));
								return true;
							}
					}
					break;
				case 700240: { // Icaronix's Box
					if (dialogActionId == USE_OBJECT) {
						if (var == 94) {
							return playQuestMovie(env, 155);
						}
					}
					break;
				}
				case 205111: // Ecus
					switch (dialogActionId) {
						case USE_OBJECT:
							if (var == 96) {
								if (isStigmaEquipped(env)) {
									return sendQuestDialog(env, 2716);
								} else {
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 1));
									return closeDialogWindow(env);
								}
							}
							return false;
						case QUEST_SELECT:
							if (var == 98) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case SELECT5_3:
							if (var == 98) {
								if (giveQuestItem(env, getStoneId(player), 1)) {
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 1));
									return true;
								}
							}
							return false;
						case SELECT6_1_1_1_1:
							if (var == 96) {
								Npc npc = (Npc) env.getVisibleObject();
								npc.getController().delete();
								spawnForFiveMinutes(212992, player.getWorldMapInstance(), (float) 191.9, (float) 267.68, 1374, (byte) 0);
								changeQuestStep(env, 96, 97); // 97
								return closeDialogWindow(env);
							}
					}
					break;
				case 203701: // Lavirintos
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 9) {
								return sendQuestDialog(env, 3398);
							}
							return false;
						case SETPRO8:
							return defaultCloseDialog(env, 9, 9, true, false); // reward
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203711) { // Miriya
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 155) {
			changeQuestStep(env, 94, 98);
			spawnForFiveMinutes(205111, env.getPlayer().getWorldMapInstance(), (float) 197.6, (float) 265.9, (float) 1374.0, (byte) 0);
		}
	}

	@Override
	public boolean onEquipItemEvent(QuestEnv env, int itemId) {
		changeQuestStep(env, 98, 96); // 96
		return closeDialogWindow(env);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 97) {
				changeQuestStep(env, 97, 8); // 8
				TeleportService.teleportTo(player, 210030000, 1, 2315.9f, 1800f, 195.2f);
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
			if (var >= 93 && var <= 98) {
				removeStigma(env);
				changeQuestStep(env, var, 2);
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
			if (player.getWorldId() != 310070000) {
				if (var >= 93 && var <= 98) {
					removeStigma(env);
					changeQuestStep(env, var, 2);
					return true;
				} else if (var == 8) {
					removeStigma(env);
					return true;
				}
			}
		}
		return false;
	}

	private int getStoneId(Player player) {
		// TODO: find out the correct stigma ids for each class on official servers
		switch (player.getCommonData().getPlayerClass()) {
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
				return 0;
		}
	}

	private boolean isStigmaEquipped(QuestEnv env) {
		Player player = env.getPlayer();
		for (Item i : player.getEquipment().getEquippedItemsAllStigma()) {
			if (i.getItemId() == getStoneId(player)) {
				return true;
			}
		}
		return false;
	}

	private void removeStigma(QuestEnv env) {
		Player player = env.getPlayer();
		for (Item item : player.getEquipment().getEquippedItemsByItemId(getStoneId(player))) {
			player.getEquipment().unEquipItem(item.getObjectId());
		}
		removeQuestItem(env, getStoneId(player), 1);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
