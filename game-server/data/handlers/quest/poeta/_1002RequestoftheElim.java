package quest.poeta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ASCENSION_MORPH;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author MrPoke, vlog, Majka
 */
public class _1002RequestoftheElim extends AbstractQuestHandler {

	public _1002RequestoftheElim() {
		super(1002);
	}

	@Override
	public void register() {
		int[] npcs = { 203076, 730007, 730010, 730008, 205000, 203067 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnEnterWorld(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203076: // Ampeis
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
				case 730007: // Forest Protector Noah
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 5) {
								return sendQuestDialog(env, 1693);
							} else if (var == 6) {
								return sendQuestDialog(env, 2034);
							} else if (var == 12) {
								return sendQuestDialog(env, 2120);
							}
							return false;
						case SELECT2_1:
							if (var == 1) {
								playQuestMovie(env, 20);
								return sendQuestDialog(env, 1353);
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2, 182200002, 1, 0, 0); // 2
						case SETPRO3:
							return defaultCloseDialog(env, 5, 6, 0, 0, 182200002, 1); // 6
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 6) {
								return checkQuestItems(env, 6, 12, false, 2120, 2205); // 12
							} else if (var == 12) {
								return sendQuestDialog(env, 2120);
							}
							return false;
						case SETPRO4:
							return defaultCloseDialog(env, 12, 13); // 13
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				case 730010: // Sleeping Elder
					if (dialogActionId == USE_OBJECT) {
						if (player.getInventory().getItemCountByItemId(182200002) == 1) {
							if (var == 2) {
								env.getVisibleObject().getController().deleteAndScheduleRespawn();
								return useQuestObject(env, 2, 4, false, false); // 4
							} else if (var == 4) {
								env.getVisibleObject().getController().deleteAndScheduleRespawn();
								return useQuestObject(env, 4, 5, false, false); // 5
							}
						}
					}
					break;
				case 730008: // Daminu
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 13) {
								return sendQuestDialog(env, 2375);
							} else if (var == 14) {
								return sendQuestDialog(env, 2461);
							}
							return false;
						case SETPRO5:
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.KARAMATIS.getId(), player);
							TeleportService.teleportTo(player, newInstance, 52, 174, 229);
							changeQuestStep(env, 13, 20); // 20
							return closeDialogWindow(env);
						case SETPRO6:
							return defaultCloseDialog(env, 14, 14, true, false); // reward
					}
					break;
				case 205000: // Belpartan
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 20) {
								player.setState(CreatureState.FLYING);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(1001);
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 1001, 0));
								ThreadPoolManager.getInstance().schedule(new Runnable() {

									@Override
									public void run() {
										changeQuestStep(env, 20, 14); // 14
										TeleportService.teleportTo(player, 210010000, 1, 603, 1537, 116, (byte) 20);
									}
								}, 43000);
								return true;
							}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203067) { // Kalio
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2716);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 310010000) {
				PacketSendUtility.sendPacket(player, new SM_ASCENSION_MORPH(1));
				return true;
			} else {
				int var = qs.getQuestVarById(0);
				if (var == 20) {
					changeQuestStep(env, 20, 13); // 13
				}
			}
		}
		return false;
	}

	@Override
	public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(env.getQuestId());
		int targetId = env.getTargetId();
		if (targetId == 730010) {
			if (qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVarById(0) != 2 && qs.getQuestVarById(0) != 4) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 1100);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 1100);
	}
}
