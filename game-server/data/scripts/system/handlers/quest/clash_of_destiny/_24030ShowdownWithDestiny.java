package quest.clash_of_destiny;

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
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Enomine
 */

public class _24030ShowdownWithDestiny extends QuestHandler {

	private final static int[] mobs = { 214591, 798346, 798344, 798342, 798345, 798343 };

	public _24030ShowdownWithDestiny() {
		super(24030);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204206, 204207, 203550, 205020, 204052 };
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
		DialogAction dialog = env.getDialog();
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204206:// Cavalorn
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0)
								return sendQuestDialog(env, 1011);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				case 204207:// Kasir
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1)
								return sendQuestDialog(env, 1352);
						}
						case SETPRO2: {
							qs.setQuestVar(2);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
					break;
				case 203550:// Munin
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2)
								return sendQuestDialog(env, 1693);
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
							if (var == 8) {
								return sendQuestDialog(env, 3739);
							}
						}
						case SETPRO3: {
							return defaultCloseDialog(env, 2, 3);
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							if (var == 3 && player.getInventory().getItemCountByItemId(182215391) == 1) {
								removeQuestItem(env, 182215391, 1);
								qs.setQuestVar(4);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
						case SETPRO5: {
							qs.setQuestVar(5);
							updateQuestStatus(env);
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(320140000);
							InstanceService.registerPlayerWithInstance(newInstance, player);
							TeleportService2.teleportTo(player, 320140000, newInstance.getInstanceId(), 52, 174, 229, (byte) 10);
							return true;
						}
						case SET_SUCCEED: {
							if (var == 8) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestSelectionDialog(env);
							}
						}
					}
					break;
				case 205020:// Hagen
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2716);
						}
						case SETPRO6: {
							// TODO: Find out, if any effect or so is getting applied to player
							player.setState(CreatureState.FLIGHT_TELEPORT);
							player.unsetState(CreatureState.ACTIVE);
							player.setFlightTeleportId(1001);
							PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 1001, 0));
							qs.setQuestVar(6);
							updateQuestStatus(env);
							return true;
						}
					}
					break;
			}
		}
		if (targetId == 204052 && qs.getStatus() == QuestStatus.REWARD) {// Vidar
			switch (dialog) {
				case USE_OBJECT: {
					return sendQuestDialog(env, 10002);
				}
				case SELECT_QUEST_REWARD: {
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				}
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
					qs.setQuestVar(7);
					updateQuestStatus(env);
					Npc mob = (Npc) QuestService.spawnQuestNpc(320140000, player.getInstanceId(), 798346, player.getX(), player.getY(), player.getZ(),
						(byte) 0);
					mob.getAggroList().addHate(player, 100);
					return true;
				}
			}
			if (var == 7) {
				qs.setQuestVar(8);
				updateQuestStatus(env);
				TeleportService2.teleportTo(player, 220010000, (float) 385, (float) 1895, (float) 327, (byte) 58);
				return defaultOnKillEvent(env, 798346, 7, 8);
			}
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
