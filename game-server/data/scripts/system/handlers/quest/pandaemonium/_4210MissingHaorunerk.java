package quest.pandaemonium;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _4210MissingHaorunerk extends QuestHandler {

	private final static int questId = 4210;

	public _4210MissingHaorunerk() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204283).addOnQuestStart(questId);
		qe.registerQuestNpc(798331).addOnTalkEvent(questId);
		qe.registerQuestNpc(798332).addOnTalkEvent(questId);
		qe.registerQuestNpc(215056).addOnKillEvent(questId);
		qe.registerQuestNpc(215080).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204283) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798332) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1011);
					}
				} else if (dialog == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 798331) {
				if (dialog == DialogAction.USE_OBJECT) {
					if (qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2) == 1) {
						return sendQuestDialog(env, 10002);
					}
				} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 1, 1, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798331) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		if (defaultOnKillEvent(env, 215056, 0, 1, 1) || defaultOnKillEvent(env, 215080, 0, 1, 2)) {
			return true;
		}
		return false;
	}
}
