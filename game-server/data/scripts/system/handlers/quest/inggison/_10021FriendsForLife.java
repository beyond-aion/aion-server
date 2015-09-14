package quest.inggison;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Talk with Brigade General Versetti (798927). Talk with Tialla (798954). Talk with Lothas (799022). Destroy Ruthless Brohums (34): Woodland Brohum
 * (215522), Woodland Brohie (215520), Thicket Brohum (215523), Thicket Brohie (215521). Talk with Lothas. Use a Taloc Fruit (182206627) to maximize
 * your power. Use Taloc's Tears (182206628) to purify the inside of Taloc (20) Vanquish Celestius (215488) to obtain its heart then take it to
 * Lothas. Report back to Tialla. Report back to Brigade General Versetti. Talk with Daminu (730008). Talk with Lodas (730019). Talk with Trajanus
 * (730024). Talk with Lothas.
 * 
 * @author vlog
 */
public class _10021FriendsForLife extends QuestHandler {

	private final static int questId = 10021;
	private final static int[] mobs = { 215522, 215520, 215523, 215521 };

	public _10021FriendsForLife() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 798927, 798954, 799022 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerQuestItem(182206627, questId);
		qe.registerQuestItem(182206628, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798927: { // Versetti
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
				case 798954: { // Tialla
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 8) {
								return sendQuestDialog(env, 3057);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 8, 8, true, false); // reward
						}
					}
					break;
				}
				case 799022: { // Lothas
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							} else if (var == 4) {
								return sendQuestDialog(env, 2375);
							} else if (var == 7) {
								return sendQuestDialog(env, 2716);
							}
						}
						case SETPRO3: {
							return defaultCloseDialog(env, 2, 3); // 3
						}
						case SETPRO5: {
							if (var == 4) {
								if (player.isInGroup2()) {
									return sendQuestDialog(env, 2546);
								} else {
									if (giveQuestItem(env, 182206627, 1) && giveQuestItem(env, 182206628, 1)) {
										WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300190000);
										InstanceService.registerPlayerWithInstance(newInstance, player);
										TeleportService2.teleportTo(player, 300190000, newInstance.getInstanceId(), 202.26694f, 226.0532f, 1098.236f, (byte) 30);
										changeQuestStep(env, 4, 5, false); // 5
										return closeDialogWindow(env);
									} else {
										PacketSendUtility.sendPacket(player, STR_MSG_FULL_INVENTORY);
										return sendQuestSelectionDialog(env);
									}
								}
							}
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 7, 8, false, 10000, 10001); // 8
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
					break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798927) { // Versetti
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
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 3) {
				int var1 = qs.getQuestVarById(1);
				if (var1 >= 0 && var1 < 9) {
					return defaultOnKillEvent(env, mobs, var1, var1 + 1, 1); // 1: 1 - 8
				} else if (var1 == 9) {
					qs.setQuestVar(4); // 4
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 300190000) {
				int var = qs.getQuestVarById(0);
				int itemId = item.getItemId();
				if (itemId == 182206627) {
					if (var == 5) {
						changeQuestStep(env, 5, 6, false); // 6
						return HandlerResult.SUCCESS; // TODO: Should not remove, but use the skill
					}
				} else if (itemId == 182206628) {
					if (var == 6) {
						int var2 = qs.getQuestVarById(2);
						if (var2 >= 0 && var2 < 19) {
							changeQuestStep(env, var2, var2 + 1, false, 2); // 2: 1 - 19
							return HandlerResult.SUCCESS;
						} else if (var2 == 19) {
							qs.setQuestVar(7); // 7
							updateQuestStatus(env);
							return HandlerResult.SUCCESS;
						}
					}
				}
			}
		}
		return HandlerResult.UNKNOWN;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() != 300190000) {
				int var = qs.getQuestVarById(0);
				if (var == 5 || var == 6 || (var == 7 && player.getInventory().getItemCountByItemId(182206602) < 1)) {
					removeQuestItem(env, 182206627, 1);
					removeQuestItem(env, 182206628, 1);
					qs.setQuestVar(4);
					updateQuestStatus(env);
					return true;
				}
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
		return defaultOnLvlUpEvent(env, 10000, true);
	}
}
