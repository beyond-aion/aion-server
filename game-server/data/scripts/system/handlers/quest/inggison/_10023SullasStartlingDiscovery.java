package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
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

/**
 * @author Nephis
 * @reworked & modified Gigi, vlog
 */
public class _10023SullasStartlingDiscovery extends QuestHandler {

	private final static int questId = 10023;

	public _10023SullasStartlingDiscovery() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 798928, 798975, 798981, 730226, 730227, 730228, 798513, 798225, 798979, 798990, 730295, 700604,
			730229 };
		qe.registerOnEnterWorld(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(216531).addOnKillEvent(questId);
		qe.registerQuestItem(182206614, questId);
		qe.addHandlerSideQuestDrop(questId, 730229, 182206614, 1, 100);
		qe.registerGetingItem(182206614, questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 12) {
				if (player.getWorldId() == 300160000) {
					qs.setQuestVar(13);
					updateQuestStatus(env);
					return true;
				}
				else {
					if (player.getInventory().getItemCountByItemId(182206613) == 0) {
						return giveQuestItem(env, 182206613, 1);
					}
				}
			}
			else if (var >= 13 && var < 16) {
				if (player.getWorldId() != 300160000) {
					changeQuestStep(env, var, 12, false);
					if (player.getInventory().getItemCountByItemId(182206613) == 0) {
						giveQuestItem(env, 182206613, 1);
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		DialogAction dialog = env.getDialog();
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798928: { // Yulia
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
					break;
				}
				case 798975: { // Sulla
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							else if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
							else if (var == 9) {
								return sendQuestDialog(env, 4080);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case SETPRO7: {
							if (defaultCloseDialog(env, 6, 7)) { // 7
							  TeleportService2.teleportToNpc(player, 798513);
							  return true;
							}
						}
						case SETPRO10: {
							return defaultCloseDialog(env, 9, 10); // 10
						}
					}
					break;
				}
				case 798981: { // Philon
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO3: {
							return defaultCloseDialog(env, 2, 3); // 3
						}
					}
					break;
				}
				case 798513: { // Machiah
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 7) {
								return sendQuestDialog(env, 3398);
							}
						}
						case SETPRO8: {
							if( defaultCloseDialog(env, 7, 8)) { // 8
							  TeleportService2.teleportToNpc(player, 798225);
							  return true;
							}
						}
					}
					break;
				}
				case 798225: { // Pyrrha
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 8) {
								return sendQuestDialog(env, 3739);
							}
						}
						case SETPRO9: {
							if (defaultCloseDialog(env, 8, 9)) { // 9
							  TeleportService2.teleportToNpc(player, 798975);
							  return true;
							}
						}
					}
					break;
				}
				case 798979: { // Gelon
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 10) {
								return sendQuestDialog(env, 1608);
							}
						}
						case SETPRO11: {
							return defaultCloseDialog(env, 10, 11); // 11
						}
					}
					break;
				}
				case 798990: { // Titus
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 11) {
								return sendQuestDialog(env, 1949);
							}
						}
						case SETPRO12: {
							return defaultCloseDialog(env, 11, 12, 182206613, 1, 0, 0); // 12
						}
					}
					break;
				}
				case 730295: { // Drakan Stone Statue
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 12) {
								return sendQuestDialog(env, 3995);
							}
						}
						case SETPRO13: {
							if (var == 12) {
								if (player.getInventory().getItemCountByItemId(182206613) > 0) {
									removeQuestItem(env, 182206613, 1);
									WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300160000);
									InstanceService.registerPlayerWithInstance(newInstance, player);
									TeleportService2.teleportTo(player, 300160000, newInstance.getInstanceId(), 744.761f, 940.3738f,
										149.28333f, (byte) 91);
									return true;
								}
								else
									return sendQuestDialog(env, 10001);
							}
						}
					}
					break;
				}
				case 730229: { // Traveller's Bag
					if (dialog == DialogAction.USE_OBJECT) {
						if (var == 15) {
							return true; // loot
						}
					}
					break;
				}
				case 730226: { // Western Petrified Mass
					if (var == 3 && dialog == DialogAction.USE_OBJECT) {
						return useQuestObject(env, 3, 4, false, 0); // 4
					}
					break;
				}
				case 730227: { // Eastern Petrified Mass
					if (var == 4 && dialog == DialogAction.USE_OBJECT) {
						return useQuestObject(env, 4, 5, false, 0); // 5
					}
					break;
				}
				case 730228: { // Southern Petrified Mass
					if (var == 5 && dialog == DialogAction.USE_OBJECT) {
						return useQuestObject(env, 5, 6, false, 0); // 6
					}
					break;
				}
				case 700604: { // Hidden Switch
					if (var == 13 && dialog == DialogAction.USE_OBJECT) {
						QuestService.addNewSpawn(300160000, player.getInstanceId(), 216531, (float) 744.752, (float) 885.824,(float) 152.78523, (byte) 30);
						return useQuestObject(env, 13, 14, false, 0); // 14
					}
					break;
				}
				case 700603: { // Hidden Library Exit
					if (var == 16 && dialog == DialogAction.USE_OBJECT) {
						TeleportService2.teleportToNpc(player, 798928);
						return true;
					}
					break;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798928) { // Yulia
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 15, 16, false); // 16
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		return  HandlerResult.fromBoolean(useQuestItem(env, item, 16, 16, true)); // reward
	}

	@Override
	public boolean onKillEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final int instanceId = player.getInstanceId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		final Npc npc = (Npc) env.getVisibleObject();

		switch (env.getTargetId()) {
			case 216531:
				if (qs.getQuestVarById(0) == 14 || qs.getQuestVarById(0) == 15) {
					QuestService.addNewSpawn(300160000, instanceId, 730229, (float) 744.752, (float) 885.824, (float) 152.78523, (byte) 30);
					qs.setQuestVarById(0, 15);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 1094, 10020 };
		return defaultOnLvlUpEvent(env, quests, true);
	}
}
