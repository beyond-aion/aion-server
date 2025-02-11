package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _3973LovesMessenger extends AbstractQuestHandler {

	public _3973LovesMessenger() {
		super(3973);
	}

	public void register() {
		qe.registerQuestNpc(203893).addOnQuestStart(questId);
		qe.registerQuestNpc(203893).addOnTalkEvent(questId);
		qe.registerQuestNpc(203792).addOnTalkEvent(questId);
		qe.registerQuestNpc(203793).addOnTalkEvent(questId);
		qe.registerQuestNpc(798391).addOnTalkEvent(questId);
		qe.registerQuestNpc(798949).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.isStartable()) {
			if (targetId == 203893) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			long collect1 = player.getInventory().getItemCountByItemId(182206116);
			long collect2 = player.getInventory().getItemCountByItemId(182206117);
			long collect3 = player.getInventory().getItemCountByItemId(182206118);
			if (targetId == 203792) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1, 182206116, 1);
				}
			} else if (targetId == 203793) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1)
							if (collect1 >= 1)
								return sendQuestDialog(env, 1693);
							else
								return defaultCloseDialog(env, 1, 0);
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2, 182206117, 1);
				}
			} else if (targetId == 798391) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 2)
							if (collect1 >= 1 && collect2 >= 1)
								return sendQuestDialog(env, 2034);
							else if (collect1 == 0)
								return defaultCloseDialog(env, 2, 0);
							else if (collect2 == 0)
								return defaultCloseDialog(env, 2, 1);
					case SETPRO3:
						return defaultCloseDialog(env, 2, 3, 182206118, 1);
				}
			} else if (targetId == 798949) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 3)
							if (collect1 >= 1 && collect2 >= 1 && collect3 >= 1)
								return sendQuestDialog(env, 2375);
							else if (collect1 == 0)
								return defaultCloseDialog(env, 3, 0);
							else if (collect2 == 0)
								return defaultCloseDialog(env, 3, 1);
							else if (collect3 == 0)
								return defaultCloseDialog(env, 3, 2);
					case SELECT_QUEST_REWARD:
						return defaultCloseDialog(env, 3, 3, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
