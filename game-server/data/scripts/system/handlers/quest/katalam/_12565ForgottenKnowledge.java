package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author cheatkiller
 *
 */
public class _12565ForgottenKnowledge extends QuestHandler {

	private final static int questId = 12565;

	public _12565ForgottenKnowledge() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(801019).addOnQuestStart(questId);
		qe.registerQuestNpc(801016).addOnTalkEvent(questId);
		qe.registerQuestNpc(730784).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801019) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 801016) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				}
				else if (dialog == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			}
			else if (targetId == 730784) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				}
				else if (dialog == DialogAction.SETPRO2) {
					giveQuestItem(env, 182213337, 1);
					qs.setQuestVar(2);
					return defaultCloseDialog(env, 2, 2, true, false);
				}
			}
		}	
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801016) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 2375);
					}
					default: {
						removeQuestItem(env, 182213337, 1);
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
}

