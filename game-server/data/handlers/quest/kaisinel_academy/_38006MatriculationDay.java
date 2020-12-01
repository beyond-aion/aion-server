package quest.kaisinel_academy;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _38006MatriculationDay extends AbstractQuestHandler {

	public _38006MatriculationDay() {
		super(38006);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203726).addOnQuestStart(questId);
		qe.registerQuestNpc(799813).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203726) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799813) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 0, 1, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799813) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
