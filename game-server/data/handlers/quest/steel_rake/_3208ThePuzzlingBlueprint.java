package quest.steel_rake;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author sky123
 */
public class _3208ThePuzzlingBlueprint extends AbstractQuestHandler {

	public _3208ThePuzzlingBlueprint() {
		super(3208);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(730195).addOnQuestStart(questId);
		qe.registerQuestNpc(730195).addOnTalkEvent(questId);
		qe.registerQuestNpc(798026).addOnTalkEvent(questId);
		qe.registerQuestNpc(203830).addOnTalkEvent(questId);
		qe.registerQuestNpc(798320).addOnTalkEvent(questId);
		qe.registerCanAct(questId, 730195);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 730195) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, QuestService.checkStartConditions(player, questId, false) ? 1011 : 1004);
				else
					return sendQuestStartDialog(env, 182209088, 1);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798026) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 203830) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1693);
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 798320) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						return defaultCloseDialog(env, 2, 2, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
		return env.getTargetId() == 730195 || super.onCanAct(env, questEventType, objects);
	}
}
