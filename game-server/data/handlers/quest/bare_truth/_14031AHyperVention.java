package quest.bare_truth;

import static com.aionemu.gameserver.model.DialogAction.*;

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
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Artur
 */
public class _14031AHyperVention extends AbstractQuestHandler {

	public _14031AHyperVention() {
		super(14031);
	}

	@Override
	public void register() {
		int[] npc_ids = { 203700, 801216, 790001, 203183, 203989, };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerQuestItem(182215388, questId);
		qe.registerQuestItem(182215389, questId);
		qe.registerQuestItem(182215390, questId);
		qe.registerQuestNpc(233878).addOnKillEvent(questId);
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
				case 203700:// Fasimedes
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							qs.setQuestVar(1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 801216:// Losthes
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO2:
							qs.setQuestVar(2);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 790001:// Pernos
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1693);
						case SETPRO3:
							if (!giveQuestItem(env, 182215388, 1))
								return true;
							qs.setQuestVar(3);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 203183:// Khidia
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SETPRO5:
							if (!giveQuestItem(env, 182215389, 1))
								return true;
							qs.setQuestVar(5);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 203989:// Tumblusen
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
							if (!giveQuestItem(env, 182215390, 1))
								return true;
							qs.setQuestVar(7);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						case SETPRO9:
							qs.setQuestVar(9);
							updateQuestStatus(env);
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.NIDALBER.getId(), player);
							TeleportService.teleportTo(player, newInstance, 274, 167, 204);
							return closeDialogWindow(env);
					}
					break;
				case 730888: // Large Teleporter
					switch (dialogActionId) {
						case QUEST_SELECT:
							env.getVisibleObject().getController().delete();
							qs.setQuestVar(11);
							updateQuestStatus(env);
							playQuestMovie(env, 888);
							spawn(730898, player, 257, 257, (float) 226.35, (byte) 95); // Broken Teleporter Device
					}
					break;
				case 730898: // Broken Large Teleporter
					switch (dialogActionId) {
						case QUEST_SELECT:
							env.getVisibleObject().getController().delete();
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							TeleportService.teleportTo(player, 110010000, 1876.29f, 1511f, 812.675f, (byte) 60, TeleportAnimation.FADE_OUT_BEAM);
					}
					break;
			}

		}
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203700)// Fasimedes
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
		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (player.isInsideItemUseZone(ZoneName.get("LF1_ITEMUSEAREA_Q14031"))) {
				if (var == 3) {
					playQuestMovie(env, 21);
					return HandlerResult.fromBoolean(useQuestItem(env, item, 3, 4, false));// 3-4
				}
			}
			if (player.isInsideItemUseZone(ZoneName.get("LF1A_ITEMUSEAREA_Q14031"))) {
				if (var == 5) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 5, 6, false));// 5-6
				}
			}
			if (player.isInsideItemUseZone(ZoneName.get("LF2_ITEMUSEAREA_Q14031"))) {
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
				// Shattered Large Teleporter
				spawn(730888, player, 257, 257, (float) 226.35, (byte) 95);
				// Captain Tarbana
				spawn(233878, player, (float) 262.9, (float) 224.5, (float) 211.348, (byte) 95);
				// 5x Baranath Sentinel
				spawn(233886, player, (float) 217.015, (float) 221.694, (float) 207.49455, (byte) 97);
				spawn(233886, player, (float) 239.732, (float) 211.250, (float) 209.19, (byte) 97);
				spawn(233886, player, (float) 257.065, (float) 204.49, (float) 209.094, (byte) 97);
				spawn(233886, player, (float) 274.899, (float) 199.398, (float) 208.83487, (byte) 97);
				spawn(233886, player, (float) 282.878, (float) 223.742, (float) 208.252, (byte) 97);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 233878)
				env.getVisibleObject().getController().delete();
			return defaultOnKillEvent(env, 233878, 9, 10);
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14030);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14030);
	}
}
