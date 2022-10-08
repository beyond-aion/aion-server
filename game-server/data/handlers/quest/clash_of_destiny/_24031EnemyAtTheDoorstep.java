package quest.clash_of_destiny;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Enomine
 */
public class _24031EnemyAtTheDoorstep extends AbstractQuestHandler {

	public _24031EnemyAtTheDoorstep() {
		super(24031);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204052, 801224, 203550, 203654, 204369 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerQuestItem(182215394, questId);
		qe.registerQuestItem(182215395, questId);
		qe.registerQuestItem(182215396, questId);
		qe.registerQuestNpc(233879).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);

	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204052:// vidar
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							qs.setQuestVar(1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 801224:// Rapidfire Rita
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO2:
							qs.setQuestVar(2);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 203550:// Munin
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1693);
						case SETPRO3:
							if (!giveQuestItem(env, 182215394, 1))
								return true;
							qs.setQuestVar(3);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 203654:// Aurtri
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SETPRO5:
							if (!giveQuestItem(env, 182215395, 1))
								return true;
							qs.setQuestVar(5);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 204369:// Tyr
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
							if (var == 8) {
								return sendQuestDialog(env, 3739);
							}
							return false;
						case SETPRO7:
							if (!giveQuestItem(env, 182215396, 1))
								return true;
							qs.setQuestVar(7);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						case SETPRO9:
							qs.setQuestVar(9);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 730888: // Teleporter Device
					switch (dialogActionId) {
						case QUEST_SELECT:
							// TODO:play movie find movie ID
							env.getVisibleObject().getController().delete();
							qs.setQuestVar(11);
							updateQuestStatus(env);
							spawn(730898, player, (float) 262.9, (float) 224.5, (float) 212.2, (byte) 95); // Broken Teleporter Device
					}
					break;
				case 730898: // Broken Teleporter
					switch (dialogActionId) {
						case QUEST_SELECT:
							env.getVisibleObject().getController().delete();
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
					}
					break;
			}

		}
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204052)// vidar
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 4083);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default: {
						return sendQuestEndDialog(env);
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
			int var = qs.getQuestVarById(0);
			if (player.isInsideItemUseZone(ZoneName.get("DF1_ITEMUSEAREA_Q24031"))) {
				if (var == 3) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 3, 4, false));// 3-4
				}
			}
			if (player.isInsideItemUseZone(ZoneName.get("DF1A_ITEMUSEAREA_Q24031"))) {
				if (var == 5) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 5, 6, false));// 5-6
				}
			}
			if (player.isInsideItemUseZone(ZoneName.get("DF2_ITEMUSEAREA_Q24031"))) {
				if (var == 7) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 7, 8, false));// 7-8
				}
			}
		}
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 9 && player.getWorldId() == 320040000) {
				spawnForFiveMinutes(730888, player.getWorldMapInstance(), (float) 262.9, (float) 224.5, (float) 211.2, (byte) 95);// Shattered Large Teleporter
				spawnForFiveMinutes(233879, player.getWorldMapInstance(), (float) 262.9, (float) 224.5, (float) 211.2, (byte) 95);// Captain Hagarkan
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 233879)
				env.getVisibleObject().getController().delete();
			return defaultOnKillEvent(env, 233879, 9, 10);
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24030);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24030);
	}
}
