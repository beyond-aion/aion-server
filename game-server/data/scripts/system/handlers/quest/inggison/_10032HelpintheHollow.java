package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Majka
 */
public class _10032HelpintheHollow extends AbstractQuestHandler {

	public _10032HelpintheHollow() {
		super(10032);
	}

	@Override
	public void register() {
		// Crosia ID: 798952
		// Tialla ID: 798954
		// Lothas ID: 799022
		// Taloc's Mirage ID: 799503
		int[] npcs = { 798952, 798954, 799022, 799503 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		qe.registerOnLogOut(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182215618, questId); // Taloc's Fruit
		qe.registerQuestItem(182215619, questId); // Taloc's Tears
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798952) { // Crosia
				if (var == 0) {
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							return defaultCloseDialog(env, var, var + 1); // 1
					}
				}
			} else if (targetId == 798954) { // Tialla
				if (var == 1) {
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO2:
							return defaultCloseDialog(env, var, var + 1); // 2
					}
				}
			} else if (targetId == 799022) { // Lothas
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 2) {
							if (player.getInventory().getItemCountByItemId(182215618) > 0) {
								return sendQuestDialog(env, 1779);
							}
							return sendQuestDialog(env, 1693);
						}
						break;
					case SETPRO3:
						if (player.isInGroup()) {
							return sendQuestDialog(env, 1864);
						} else if (giveQuestItem(env, 182215618, 1) && giveQuestItem(env, 182215619, 1)) {
							changeQuestStep(env, 2, 3); // 3
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.TALOCS_HOLLOW.getId(), player);
							TeleportService.teleportTo(player, newInstance, 202.26694f, 226.0532f, 1098.236f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
							return closeDialogWindow(env);
						} else {
							PacketSendUtility.sendPacket(player, STR_MSG_FULL_INVENTORY());
							return sendQuestSelectionDialog(env);
						}
					case SETPRO4:
						changeQuestStep(env, 2, 3); // 3
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.TALOCS_HOLLOW.getId(), player);
						TeleportService.teleportTo(player, newInstance, 202.26694f, 226.0532f, 1098.236f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
						return closeDialogWindow(env);
				}
			} else if (targetId == 799503) { // Taloc's Mirage
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 6) {
							return sendQuestDialog(env, 3057);
						}
						break;
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 6, 7, false, 10000, 10001);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798952) { // Crosia
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
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
				int var1 = qs.getQuestVarById(1);
				if (itemId == 182215618) { // quest_20032b
					changeQuestStep(env, 4, 5); // 7
					return HandlerResult.SUCCESS; // //TODO: Should return FAILED (not removed, but skill still should be used)
				} else if (itemId == 182215619) { // quest_20032a
					if (var == 5) {
						if (var1 >= 0 && var1 < 19) {
							changeQuestStep(env, var1, var1 + 1, false, 1); // 3: 19
							return HandlerResult.SUCCESS;
						} else if (var1 == 19) {
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
			if (player.getWorldId() == 300190000) {
				qs.setQuestVar(4);
				updateQuestStatus(env);
				return true;
			} else {
				int var = qs.getQuestVarById(0);
				if (var >= 3 && var < 7) {
					restoreStep(env);
					return true;
				} else if (var == 7) { // Final boss killed
					removeQuestItem(env, 182215618, 1);
					removeQuestItem(env, 182215619, 1);
					qs.setQuestVar(8); // Reward
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		return restoreStep(env);
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		return restoreStep(env);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10031);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10031);
	}

	private boolean restoreStep(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var >= 3 && var <= 7) {
				removeQuestItem(env, 182215618, 1);
				removeQuestItem(env, 182215619, 1);
				qs.setQuestVar(2);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
