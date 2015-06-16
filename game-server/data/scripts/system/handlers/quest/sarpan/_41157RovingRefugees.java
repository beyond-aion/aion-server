package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author zhkchi
 *
 */
public class _41157RovingRefugees extends QuestHandler {

	private final static int questId = 41157;

	public _41157RovingRefugees() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205583).addOnQuestStart(questId);
		qe.registerQuestNpc(205583).addOnTalkEvent(questId);
		qe.registerQuestNpc(205990).addOnTalkEvent(questId);
		qe.registerQuestNpc(205991).addOnTalkEvent(questId);
		qe.registerQuestNpc(205992).addOnTalkEvent(questId);
		qe.registerQuestNpc(205572).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 205583) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205990) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); 
				}
			}else if (targetId == 205991) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1693);
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2); 
				}
			}else if (targetId == 205992) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2034);
					case SETPRO3:
						return defaultCloseDialog(env, 2, 3); 
				}
			}
			else if (targetId == 205583) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 3, 3, true);
						return sendQuestDialog(env, 5);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205583)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}