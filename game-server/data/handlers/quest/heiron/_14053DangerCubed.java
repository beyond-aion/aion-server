package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Artur, Majka
 */
public class _14053DangerCubed extends AbstractQuestHandler {

	public _14053DangerCubed() {
		super(14053);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(204020).addOnTalkEvent(questId);
		qe.registerQuestNpc(204602).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14050);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14050);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204602)
				return sendQuestEndDialog(env);
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204020) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					break;
				case SETPRO2:
					if (var == 1) {
						changeQuestStep(env, 1, 2); // 2
						TeleportService.teleportTo(player, WorldMapType.HEIRON.getId(), 2010.1975f, 1395.4108f, 118.125f, (byte) 62,
							TeleportAnimation.FADE_OUT_BEAM);
						return closeDialogWindow(env);
					}
			}
		} else if (targetId == 204602) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					else if (var == 2)
						return sendQuestDialog(env, 1693);
					else if (var == 3)
						return sendQuestDialog(env, 2034);
					break;
				case SELECT3_1:
					playQuestMovie(env, 191);
					break;
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						qs.setStatus(QuestStatus.REWARD);
						qs.setRewardGroup(0);
						updateQuestStatus(env);
						return sendQuestEndDialog(env);
					} else
						return sendQuestDialog(env, 10001);
				case SETPRO1:
					if (var == 0) {
						changeQuestStep(env, 0, 1); // 1
						TeleportService.teleportTo(player, WorldMapType.ELTNEN.getId(), 1596.1948f, 1529.9152f, 317, (byte) 120,
							TeleportAnimation.FADE_OUT_BEAM);
						return closeDialogWindow(env);
					}
					break;
				case SETPRO3:
					return defaultCloseDialog(env, 2, 3); // 3
			}
		}
		return false;
	}
}
