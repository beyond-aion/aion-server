package quest.silentera_canyon;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _30155StonetoFlesh extends AbstractQuestHandler {

	public _30155StonetoFlesh() {
		super(30155);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799234).addOnQuestStart(questId);
		qe.registerQuestNpc(799234).addOnTalkEvent(questId);
		qe.registerQuestNpc(204433).addOnTalkEvent(questId);
		qe.registerQuestNpc(204304).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799234) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204304) {
				if (var == 3)
					return sendQuestEndDialog(env);
			} else if (targetId == 799234) {
				return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 799234) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
						return false;
					case SETPRO2:
						if (var == 1) {
							changeQuestStep(env, 1, 2);
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SELECT_QUEST_REWARD:
						if (var == 2) {
							removeQuestItem(env, 182209252, 1);
							qs.setRewardGroup(0);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 6);
						}
				}
			} else if (targetId == 204433) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						return false;
					case SETPRO1:
						if (var == 0)
							return defaultCloseDialog(env, 0, 1, 182209252, 1);
				}
			} else if (targetId == 204304) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 2034);
						return false;
					case SETPRO3:
						if (var == 1)
							changeQuestStep(env, 1, 3);
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						if (var == 3) {
							removeQuestItem(env, 182209252, 1);
							qs.setRewardGroup(1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
						}
				}
			}
		}
		return false;
	}
}
