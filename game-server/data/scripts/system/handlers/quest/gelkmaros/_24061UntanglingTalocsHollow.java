package quest.gelkmaros;

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
 * @author Ritsu
 */
public class _24061UntanglingTalocsHollow extends QuestHandler {

	private final static int questId = 24061;
	private final static int[] npcs = { 799226, 799247, 799250, 799325, 799503, 799239 };

	public _24061UntanglingTalocsHollow() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		qe.registerQuestNpc(215488).addOnKillEvent(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182207604, questId);
		qe.registerQuestItem(182207603, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799226) { // Valetta
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
			} else if (targetId == 799247) { // Angrad
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO2: {
						return defaultCloseDialog(env, 1, 2); // 2
					}
				}
			} else if (targetId == 799250) { // Eddas
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 2)
							return sendQuestDialog(env, 1693);
					}
					case SETPRO3: {
						return defaultCloseDialog(env, 2, 3); // 3
					}
				}
			} else if (targetId == 799325) { // Taloc's Guardian
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 3) {
							return sendQuestDialog(env, 2034);
						} else if (var == 4) {
							return sendQuestDialog(env, 2120);
						} else if (var == 5) {
							return sendQuestDialog(env, 2716);
						}
					}
					case SETPRO4: {
						if (player.isInGroup2()) {
							return sendQuestDialog(env, 2717);
						} else {
							if (giveQuestItem(env, 182207603, 1) && giveQuestItem(env, 182207604, 1)) {
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300190000);
								InstanceService.registerPlayerWithInstance(newInstance, player);
								TeleportService2.teleportTo(player, 300190000, newInstance.getInstanceId(), 202.26694f, 226.0532f, 1098.236f, (byte) 30);
								changeQuestStep(env, 3, 4, false); // 4
								return closeDialogWindow(env);
							} else {
								PacketSendUtility.sendPacket(player, STR_MSG_FULL_INVENTORY);
								return sendQuestSelectionDialog(env);
							}
						}
					}
					case FINISH_DIALOG: {
						return sendQuestSelectionDialog(env);
					}
				}
			} else if (targetId == 799503) { // Taloc's Mirage
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 7) {
							return sendQuestDialog(env, 3398);
						}
					}
					case SETPRO8: {
						return defaultCloseDialog(env, 7, 8); // 8
					}
				}
			} else if (targetId == 799239) { // Vellun
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 8)
							return sendQuestDialog(env, 1608);
					}
					case SET_SUCCEED: {
						return defaultCloseDialog(env, 8, 8, true, false); // reward
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799226) { // Valetta
				if (dialog == DialogAction.QUEST_SELECT) {
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
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int targetId = env.getTargetId();
			switch (targetId) {
				case 215488: { // Celestius
					return defaultOnKillEvent(env, 215488, 6, 7); // 7
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 300190000) {
				int itemId = item.getItemId();
				int var = qs.getQuestVarById(0);
				int var3 = qs.getQuestVarById(3);
				if (itemId == 182207604) { // Taloc Fruit
					changeQuestStep(env, 4, 5, false); // 5
					return HandlerResult.SUCCESS; // //TODO: Should return FAILED (not removed, but skill still should be used)
				} else if (itemId == 182207603) { // Taloc's Tears
					if (var == 5) {
						if (var3 >= 0 && var3 < 19) {
							changeQuestStep(env, var3, var3 + 1, false, 3); // 3: 19
							return HandlerResult.SUCCESS;
						} else if (var3 == 19) {
							qs.setQuestVar(6);
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
				if (var > 3 && var < 7) {
					removeQuestItem(env, 182207604, 1);
					removeQuestItem(env, 182207603, 1);
					qs.setQuestVar(3);
					updateQuestStatus(env);
					return true;
				} else if (var == 7) { // Final boss killed
					removeQuestItem(env, 182207604, 1);
					removeQuestItem(env, 182207603, 1);
					qs.setQuestVar(10);
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
		return defaultOnLvlUpEvent(env, 24060, true);
	}
}
