package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 *
 */
 
public class _41258BasicRestoration extends QuestHandler {

	private final static int	questId	= 41258;

	public _41258BasicRestoration() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205756).addOnQuestStart(questId);
		qe.registerQuestNpc(205756).addOnTalkEvent(questId);
		qe.registerQuestNpc(730477).addOnTalkEvent(questId);
		qe.registerQuestNpc(218661).addOnKillEvent(questId);
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 218661, 0, 1);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205756) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 730477) {
				if (dialog == DialogAction.USE_OBJECT) {
					if(qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1352);
				}
				else if (dialog == DialogAction.SET_SUCCEED) {
					return defaultCloseDialog(env, 1, 1, true, false);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205756) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
