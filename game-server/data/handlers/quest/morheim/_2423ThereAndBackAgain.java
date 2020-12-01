package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Cheatkiller
 */
public class _2423ThereAndBackAgain extends AbstractQuestHandler {

	public _2423ThereAndBackAgain() {
		super(2423);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204326).addOnQuestStart(questId);
		qe.registerQuestNpc(204375).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 204326) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204375:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 0)
								return sendQuestDialog(env, 1011);
							else if (qs.getQuestVarById(0) == 1)
								return sendQuestDialog(env, 1352);
							else if (qs.getQuestVarById(0) == 2)
								return sendQuestDialog(env, 1693);
							return false;
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 1, 2, false, 10000, 10001);
						}
						case SETPRO3: {
							TeleportService.teleportTo(player, 210020000, 1, 370.13f, 2682.59f, 171, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
							qs.setQuestVar(3);
							return defaultCloseDialog(env, 3, 3, true, false);
						}
						case SELECT3_2: {
							return sendQuestDialog(env, 1779);
						}
						case SETPRO1: {
							TeleportService.teleportTo(player, 210020000, 1, 370.13f, 2682.59f, 171, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
							return defaultCloseDialog(env, 0, 1);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204375) {
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
