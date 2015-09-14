package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author cheatkiller
 */
public class _22820TheMissingMessenger extends QuestHandler {

	private final static int questId = 22820;

	public _22820TheMissingMessenger() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801243).addOnQuestStart(questId);
		qe.registerQuestNpc(801243).addOnTalkEvent(questId);
		qe.registerQuestNpc(801248).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801243) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 801248) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialog == DialogAction.SETPRO1) {
					giveQuestItem(env, 182213472, 1);
					qs.setQuestVar(1);
					return defaultCloseDialog(env, 1, 1, true, false);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801243) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 2375);
					}
					default: {
						removeQuestItem(env, 182213472, 1);
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
}
