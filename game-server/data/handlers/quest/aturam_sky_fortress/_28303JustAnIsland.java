package quest.aturam_sky_fortress;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author zhkchi, Pad
 */
public class _28303JustAnIsland extends AbstractQuestHandler {

	public _28303JustAnIsland() {
		super(28303);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799530).addOnQuestStart(questId);
		qe.registerQuestNpc(799530).addOnTalkEvent(questId);
		qe.registerQuestNpc(730390).addOnTalkEvent(questId);
		qe.registerQuestNpc(700980).addOnTalkEvent(questId);
		qe.registerQuestNpc(804821).addOnTalkEvent(questId);
		qe.registerQuestNpc(217382).addOnKillEvent(questId);
		qe.registerQuestNpc(217376).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799530) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else if (dialogActionId == QUEST_ACCEPT_1) {
					playQuestMovie(env, 470);
					return sendQuestStartDialog(env);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 730390) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case USE_OBJECT:
						return sendQuestDialog(env, 1007);
					case SETPRO1:
						TeleportService.teleportTo(player, 300240000, 158.88f, 624.42f, 901f, (byte) 20);
						return closeDialogWindow(env);
					default:
						return sendQuestStartDialog(env);
				}
			} else if (targetId == 700980) {
				return useQuestObject(env, 2, 3, true, true);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 804821) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == 0)
				return defaultOnKillEvent(env, 217382, 0, 1);
			else
				return defaultOnKillEvent(env, 217376, 1, 2);
		}
		return false;

	}
}
