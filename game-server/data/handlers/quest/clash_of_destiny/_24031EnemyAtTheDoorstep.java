package quest.clash_of_destiny;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Enomine
 */
public class _24031EnemyAtTheDoorstep extends AbstractQuestHandler {

	public _24031EnemyAtTheDoorstep() {
		super(24031);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204052, 801224, 203550, 203654, 204369, 730888, 730898 };
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
							return defaultCloseDialog(env, 0, 1);
					}
					break;
				case 801224:// Rapidfire Rita
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					break;
				case 203550:// Munin
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1693);
						case SETPRO3:
							if (!giveQuestItem(env, 182215394, 1))
								return true;
							return defaultCloseDialog(env, 2, 3);
					}
					break;
				case 203654:// Aurtri
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SETPRO5:
							if (!giveQuestItem(env, 182215395, 1))
								return true;
							return defaultCloseDialog(env, 4, 5);
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
							return defaultCloseDialog(env, 6, 7);
						case SETPRO9:
							TeleportService.teleportTo(player, player.getWorldMapInstance(), 3028.1926f, 873.0997f, 362.875f, (byte) 15, TeleportAnimation.FADE_OUT_BEAM);
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
						TeleportService.teleportTo(player, 120010000, 1275.37f, 1169.885f, 215.21492f, (byte) 90, TeleportAnimation.FADE_OUT_BEAM);
						return true;
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
			if (player.isInsideItemUseZone("DF1_ITEMUSEAREA_Q24031")) {
				if (var == 3) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 3, 4, false));// 3-4
				}
			}
			if (player.isInsideItemUseZone("DF1A_ITEMUSEAREA_Q24031")) {
				if (var == 5) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 5, 6, false));// 5-6
				}
			}
			if (player.isInsideItemUseZone("DF2_ITEMUSEAREA_Q24031")) {
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
			// Captain Hagarkan
			spawn(233879, player, 262.9f, 224.5f, 211.2f, (byte) 95);
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
