package quest.time_of_return;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Enomine
 */

public class _14095PreparingForTheFuture extends QuestHandler {

	private final static int questId = 14095;
	private final static int[] npc_ids = { 801327, 802178, 802059, 205842, 203700, 800527 };

	public _14095PreparingForTheFuture() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerQuestItem(182215416, questId);
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
				case 801327:// Kaisinel
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1011);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				case 802178:// Oriata of the Past
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO2: {
							if (!giveQuestItem(env, 182215416, 1))
								return true;
							Npc npc = (Npc) env.getVisibleObject();
							if (targetId == 802178)
								NpcActions.delete(npc);
							qs.setQuestVar(2);
							updateQuestStatus(env);
							return defaultCloseDialog(env, 2, 3);
						}
					}
					break;
				case 802059:// Protector Oriata
					switch (dialog) {
						case USE_OBJECT: {
							if (var == 1) {
								TeleportService2.teleportTo(player, 300330000, player.getInstanceId(), 244, 244, 125, (byte) 10);
							}
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO5: {
							if (var == 4)
								qs.setQuestVar(5);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
				case 205842:// Ancarnus
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2716);
						}
						case SETPRO6: {
							TeleportService2.teleportTo(player, 110010000, player.getInstanceId(), 1873, 1511, 813, (byte) 118);
							qs.setQuestVar(6);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
				case 203700:// Fasimedes
					switch (dialog) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 3057);
						}
						case SETPRO7: {
							qs.setQuestVar(7);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
					break;
			}
		}
		if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800527) {// Tirins
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 3398);
					}
					case SELECT_QUEST_REWARD: {
						return sendQuestDialog(env, 5);
					}
					default: {
						return sendQuestEndDialog(env);
					}
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
			int var = qs.getQuestVars().getQuestVars();
			if (var == 1 && player.getWorldId() == 300330000) {
				QuestService.addNewSpawn(300330000, player.getInstanceId(), 802178, (float) 245.54, (float) 245.5, 125, (byte) 95);// Oriata of the Past
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int var = qs.getQuestVarById(0);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("IDLDF4A_ITEMUSEAREA_Q14095"))) {
				if (var == 3) {
					TeleportService2.teleportTo(player, 600030000, player.getInstanceId(), 304, 1718, 296, (byte) 48);
					return HandlerResult.fromBoolean(useQuestItem(env, item, 3, 4, false));// 3-4
				}
			}
		}
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}

}
