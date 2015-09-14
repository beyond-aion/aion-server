package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _24071TheProtectorsTest extends QuestHandler {

	private final static int questId = 24071;

	private static int[] shulMobs = { 217913, 217912 };

	private static int[] marbataMobs = { 217914 };

	private static int[] drakanMobs = { 218100, 218098 };

	public _24071TheProtectorsTest() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182215402, questId);
		int[] npcs = { 205585, 205987, 802058, 205754, 205743, 205756, 702088, 730493 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		for (int mob : shulMobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int mob : marbataMobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int mob : drakanMobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerOnLevelUp(questId);
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
			if (targetId == 205585) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1); // 1
					}
				}
			} else if (targetId == 205987) {
				int var3 = qs.getQuestVarById(3);
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						} else if (var == 7 && var3 == 6) {
							return sendQuestDialog(env, 3398);
						}
					}
					case SETPRO2:
						oriataEvent(env, player);
						return closeDialogWindow(env);
					case SETPRO8: {
						oriataEvent(env, player);
						return closeDialogWindow(env);
					}
				}
			} else if (targetId == 802058) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 2) {
							return sendQuestDialog(env, 1693);
						} else if (var == 8) {
							return sendQuestDialog(env, 3739);
						}
					}
					case SETPRO3: {
						TeleportService2.teleportTo(player, 600020000, 943.0057f, 2214.2717f, 532.5044f, (byte) 53);
						changeQuestStep(env, 2, 3, false); // 3
						return closeDialogWindow(env);
					}
					case SETPRO9: {
						giveQuestItem(env, 182215402, 1);
						return defaultCloseDialog(env, 8, 9);
					}
				}
			} else if (targetId == 205754) {
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
			} else if (targetId == 205743) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 4) {
							return sendQuestDialog(env, 2375);
						}
						if (var == 5) {
							return sendQuestDialog(env, 2716);
						}
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, 4, 5, false, 10000, 10001);
					}
					case SETPRO6: {
						return defaultCloseDialog(env, 5, 6);
					}
				}
			} else if (targetId == 205756) {
				int var1 = qs.getQuestVarById(1);
				int var2 = qs.getQuestVarById(2);
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 6 && var1 == 5 && var2 == 3) {
							return sendQuestDialog(env, 3057);
						}
					}
					case SETPRO7: {
						return defaultCloseDialog(env, 6, 7);
					}
				}
			} else if (targetId == 702088) {
				return true; // looting
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 802058) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 4080);
				else if (dialog == DialogAction.SELECT_ACTION_4081) {
					playQuestMovie(env, 889);
					return sendQuestDialog(env, 4081);
				} else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (player.isInsideZone(ZoneName.get("IDLDF4A_ITEMUSEAREA_Q14071"))) {
			return HandlerResult.fromBoolean(useQuestItem(env, item, 9, 9, true, 0));
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == 6) {
				return defaultOnKillEvent(env, shulMobs, 0, 5, 1) || defaultOnKillEvent(env, marbataMobs, 0, 3, 2);
			} else if (qs.getQuestVarById(0) == 7) {
				return defaultOnKillEvent(env, drakanMobs, 0, 6, 3);
			}
		}
		return false;
	}

	public void oriataEvent(QuestEnv env, Player player) {
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int var = qs.getQuestVarById(0);
		WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300330000);
		InstanceService.registerPlayerWithInstance(newInstance, player);
		TeleportService2.teleportTo(player, 300330000, newInstance.getInstanceId(), 233, 242, 125);
		if (var == 1)
			changeQuestStep(env, 1, 2, false); // 2
		if (var == 7)
			changeQuestStep(env, 7, 8, false); // 8
		QuestService.addNewSpawn(300330000, player.getInstanceId(), 802058, 248.84947f, 249.30412f, 125.063126f, (byte) 60);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 24070);
	}
}
