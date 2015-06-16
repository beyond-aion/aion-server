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
 * @author Cheatkiller
 *
 */
public class _14061HolyProblems extends QuestHandler {

	private final static int questId = 14061;

	public _14061HolyProblems() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 798927, 798954, 799022 };
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
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
							}
							else if (var == 25) {
								return sendQuestDialog(env, 3057);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 25, 26, true, false); // reward
						}
					}
					break;
				}
				case 799022: { // Lothas
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							else if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
							else if (var == 24) {
								return sendQuestDialog(env, 2716);
							}
						}
						case SETPRO3: {
							if (var == 2) {
								if (player.isInGroup2()) {
									return sendQuestDialog(env, 2546);
								}
								else {
									if (giveQuestItem(env, 182206627, 1) && giveQuestItem(env, 182206628, 1)) {
										WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300190000);
										InstanceService.registerPlayerWithInstance(newInstance, player);
										TeleportService2.teleportTo(player, 300190000, newInstance.getInstanceId(), 202.26694f, 226.0532f,
											1098.236f, (byte) 30);
										changeQuestStep(env, 2, 3, false);
										return closeDialogWindow(env);
									}
									else {
										PacketSendUtility.sendPacket(player, STR_MSG_FULL_INVENTORY);
										return sendQuestSelectionDialog(env);
									}
								}
							}
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 24, 25, false, 10000, 10001);
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
					break;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798927) { // Versetti
				if (dialog == DialogAction.USE_OBJECT) {
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
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 300190000) {
				int var = qs.getQuestVarById(0);
				int itemId = item.getItemId();
				if (itemId == 182206627) {
					if (var == 3) {
						changeQuestStep(env, 3, 4, false);
						return HandlerResult.SUCCESS;
					}
				}
				else if (itemId == 182206628) {
					if (var >= 4 && var < 24) {
						changeQuestStep(env, var, var + 1, false);
						return HandlerResult.SUCCESS;
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
				if (var <= 3 && var >= 24 && player.getInventory().getItemCountByItemId(182215346) == 0) {
					removeQuestItem(env, 182206627, 1);
					removeQuestItem(env, 182206628, 1);
					qs.setQuestVar(2);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 14060);
	}
}
