package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Majka
 */
public class _10034FoundUnderground extends AbstractQuestHandler {

	public _10034FoundUnderground() {
		super(10034);
	}

	@Override
	public void register() {
		// Sueros ID: 799030
		// Honeus ID: 799029
		// Titus ID: 798990
		// Drakan Stone Statue ID: 730295
		// Hidden Switch ID: 700604
		// Traveler's bag ID: 730229
		int[] npcs = { 799030, 799029, 798990, 730295, 700604, 730229 };
		qe.addHandlerSideQuestDrop(questId, 730229, 182215628, 1, 100);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnEnterWorld(questId);
		qe.registerQuestItem(182215628, questId);
		qe.registerQuestNpc(216531).addOnKillEvent(questId);
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int dialogActionId = env.getDialogActionId();
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799030: // Sueros
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					break;
				case 799029:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					break;
				case 798990:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						case SELECT3_1_1:
							playQuestMovie(env, 504);
							return sendQuestDialog(env, 1695);
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3);
					}
					break;
				case 730295:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							break;
						case SETPRO4:
							if (var == 3) {
								if (removeQuestItem(env, 182215627, 1)) {
									WorldMapInstance instance = InstanceService.getNextAvailableInstance(WorldMapType.UDAS_TEMPLE_LOWER.getId(), player);
									TeleportService.teleportTo(player, instance, 795.28143f, 918.806f, 149.80243f, (byte) 73, TeleportAnimation.FADE_OUT_BEAM);
									return true;
								} else {
									return sendQuestDialog(env, 10001);
								}
							}
							break;
					}
					break;
				case 700604:
					if (var == 4 && dialogActionId == USE_OBJECT) {
						InstanceHandler instanceHandler = player.getWorldMapInstance().getInstanceHandler();
						instanceHandler.handleUseItemFinish(player, (Npc) env.getVisibleObject());
						return useQuestObject(env, 4, 5, false, 0);
					}
					break;
				case 730229:
					if (var == 6) {
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 3057);
						}

						if (dialogActionId == SETPRO7) {
							giveQuestItem(env, 182215628, 1);
							return defaultCloseDialog(env, 6, 7);
						}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799030) {
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
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
			if (var == 3) {
				if (player.getWorldId() == 300160000) {
					qs.setQuestVar(4);
					updateQuestStatus(env);
					return true;
				}
			} else if (var >= 4 && var < 7) {
				if (player.getWorldId() != 300160000) {
					qs.setQuestVar(3);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		switch (env.getTargetId()) {
			case 216531:
				if (var == 5) {
					spawnForFiveMinutes(730229, player.getWorldMapInstance(), 744, 885, 154, (byte) 90); // Traveler's Bag
					spawnForFiveMinutes(804815, player.getWorldMapInstance(), 776, 876, 151, (byte) 90);
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (item.getItemId() == 182215628) {
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				qs.setQuestVar(8);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
			}
			return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10031);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10031);
	}
}
