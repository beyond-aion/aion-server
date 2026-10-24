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

/**
 * @author Artur
 */
public class _14031AHyperVention extends AbstractQuestHandler {

	public _14031AHyperVention() {
		super(14031);
	}

	@Override
	public void register() {
		int[] npc_ids = { 203700, 801216, 790001, 203183, 203989, 730888, 730898 };
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
							return defaultCloseDialog(env, 0, 1);
					}
					break;
				case 801216:// Losthes
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					break;
				case 790001:// Pernos
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1693);
						case SETPRO3:
							if (!giveQuestItem(env, 182215388, 1))
								return true;
							return defaultCloseDialog(env, 2, 3);
					}
					break;
				case 203183:// Khidia
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SETPRO5:
							if (!giveQuestItem(env, 182215389, 1))
								return true;
							return defaultCloseDialog(env, 4, 5);
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
							return defaultCloseDialog(env, 6, 7);
						case SETPRO9:
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.NIDALBER.getId(), player);
							TeleportService.teleportTo(player, newInstance, 274.6711f, 168.1845f, 204.3471f, (byte) 35, TeleportAnimation.FADE_OUT_BEAM);
							return defaultCloseDialog(env, 8, 9);
					}
					break;
				case 730888: // Large Teleporter
					if (dialogActionId == QUEST_SELECT && changeQuestStep(env, 10, 11)) {
						playQuestMovie(env, 888);
						env.getVisibleObject().getController().delete();
						spawn(730898, player, 257, 257, 226.35f, (byte) 95); // Broken Large Teleporter
						return true;
					}
					break;
				case 730898: // Broken Large Teleporter
					if (dialogActionId == QUEST_SELECT && defaultCloseDialog(env, 11, 11, true, false)) {
						TeleportService.teleportTo(player, 110010000, 1876.29f, 1511f, 812.675f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
						return true;
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
			if (player.isInsideItemUseZone("LF1_ITEMUSEAREA_Q14031")) {
				if (var == 3) {
					playQuestMovie(env, 21);
					return HandlerResult.fromBoolean(useQuestItem(env, item, 3, 4, false));// 3-4
				}
			}
			if (player.isInsideItemUseZone("LF1A_ITEMUSEAREA_Q14031")) {
				if (var == 5) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 5, 6, false));// 5-6
				}
			}
			if (player.isInsideItemUseZone("LF2_ITEMUSEAREA_Q14031")) {
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
		if (player.getWorldId() != 320040000 || !player.getWorldMapInstance().getNpcs(730888, 730898).isEmpty())
			return false;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		int var = qs.getQuestVars().getQuestVars();
		if (var == 9 || var == 10)
			spawn(730888, player, 257, 257, 226.35f, (byte) 95); // Large Teleporter
		else if (var == 11)
			spawn(730898, player, 257, 257, 226.35f, (byte) 95); // Broken Large Teleporter
		if (var == 9) {
			// Captain Tarbana
			spawn(233878, player, 262.9f, 224.5f, 211.348f, (byte) 95);
			// 5x Baranath Sentinel
			spawn(233886, player, 217.015f, 221.694f, 207.49455f, (byte) 97);
			spawn(233886, player, 239.732f, 211.250f, 209.19f, (byte) 97);
			spawn(233886, player, 257.065f, 204.49f, 209.094f, (byte) 97);
			spawn(233886, player, 274.899f, 199.398f, 208.83487f, (byte) 97);
			spawn(233886, player, 282.878f, 223.742f, 208.252f, (byte) 97);
		}
		return true;
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
