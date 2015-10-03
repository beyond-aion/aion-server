package quest.heiron;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Artur
 */
public class _14053DangerCubed extends QuestHandler {

	private final static int questId = 14053;

	public _14053DangerCubed() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(204020).addOnTalkEvent(questId);
		qe.registerQuestNpc(204501).addOnTalkEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 14050, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204501)
				return sendQuestEndDialog(env);
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204020) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
				case SELECT_ACTION_1353:
					playQuestMovie(env, 191);
					break;
				case SETPRO2:
					if (var == 1) {
						changeQuestStep(env, 1, 2, false); // 2
						TeleportService2.teleportTo(player, WorldMapType.HEIRON.getId(), 2455.827f, 243.22636f, 421.75235f, (byte) 92,
							TeleportAnimation.FADE_OUT_BEAM);
						return closeDialogWindow(env);
					}
			}
		} else if (targetId == 204501) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					else if (var == 2)
						return sendQuestDialog(env, 1693);
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					} else
						return sendQuestDialog(env, 10001);
				case SETPRO1:
					if (var == 0) {
						changeQuestStep(env, 0, 1, false); // 1
						TeleportService2.teleportTo(player, WorldMapType.ELTNEN.getId(), 1596.1948f, 1529.9152f, 317, (byte) 120,
							TeleportAnimation.FADE_OUT_BEAM);
						return closeDialogWindow(env);
					}
				case SETPRO3:
					return defaultCloseDialog(env, 2, 3); // 3
			}
		}
		return false;
	}
}
