package quest.fort_tiamat;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
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
 * @author Cheatkiller
 */
public class _10070KaisinelsCommand extends QuestHandler {

	private final static int questId = 10070;

	public _10070KaisinelsCommand() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 205579, 798600, 205842, 730628, 730691, 800386, 800431, 800352 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205579) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						} else if (var == 2) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1); // 1
					}
					case SETPRO3: {
						removeQuestItem(env, 182213241, 1);
						return defaultCloseDialog(env, 2, 3);
					}
				}
			} else if (targetId == 798600) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO2: {
						giveQuestItem(env, 182213241, 1);
						return defaultCloseDialog(env, 1, 2);
					}
				}
			} else if (targetId == 205842) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 3) {
							return sendQuestDialog(env, 2034);
						}
					}
					case SETPRO4: {
						return defaultCloseDialog(env, 3, 4);
					}
				}
			} else if (targetId == 730628) {
				switch (dialog) {
					case USE_OBJECT: {
						if (var == 4) {
							return sendQuestDialog(env, 2375);
						}
					}
					case SETPRO5: {
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300490000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 300490000, newInstance.getInstanceId(), 510.96f, 516.55f, 240.26f, (byte) 60);
						changeQuestStep(env, 4, 5, false);
						return closeDialogWindow(env);
					}
				}
			} else if (targetId == 730691) {
				switch (dialog) {
					case USE_OBJECT: {
						if (var == 5) {
							return sendQuestDialog(env, 2716);
						}
					}
					case SETPRO6: {
						TeleportService2.teleportTo(player, 300490000, player.getInstanceId(), 498.9f, 509.04f, 417.4f, (byte) 60);
						QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 800386, 459.24f, 514.55f, 417.4f, (byte) 0);
						changeQuestStep(env, 5, 6, false);
						return closeDialogWindow(env);
					}
				}
			} else if (targetId == 800386) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 6) {
							return sendQuestDialog(env, 3057);
						}
					}
					case SETPRO7: {
						playQuestMovie(env, 493);
						QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 800431, 502.25f, 510.4f, 417.4f, (byte) 50);
						QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 800352, 504.21f, 514.84f, 417.4f, (byte) 60);
						QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 800387, 450.88f, 514.55f, 417.4f, (byte) 0);
						Npc npc = (Npc) env.getVisibleObject();
						npc.getController().onDelete();
						return defaultCloseDialog(env, 6, 7);
					}
				}
			} else if (targetId == 800431) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 7) {
							return sendQuestDialog(env, 3398);
						}
					}
					case SETPRO8: {
						// TODO
						return defaultCloseDialog(env, 7, 8);
					}
				}
			} else if (targetId == 800352) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 8) {
							return sendQuestDialog(env, 3739);
						}
					}
					case SET_SUCCEED: {
						TeleportService2.teleportTo(player, 600030000, 101.47f, 1612.36f, 295.989f, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
						return defaultCloseDialog(env, 8, 9, true, false);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205842) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
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
			if (player.getWorldId() != 300490000) {
				int var = qs.getQuestVarById(0);
				if (var >= 5) {
					qs.setQuestVar(4);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId)
						.getName()));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 10064);
	}
}
