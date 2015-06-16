package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;


/**
 * @author Cheatkiller
 *
 */
public class _2477ADishForDukar extends QuestHandler {

	private final static int questId = 2477;
	
	public _2477ADishForDukar() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(204355).addOnQuestStart(questId);
		qe.registerQuestNpc(204355).addOnTalkEvent(questId);
		qe.registerQuestNpc(204100).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204355) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					changeQuestStep(env, 0, 1, false);
					return sendQuestDialog(env, 1003);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204355) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				}
				else if(dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
				  return checkItemExistence(env, 1, 2, false, 182204196, 5, true, 10000, 10001, 182204234, 1);
				}
				else if(dialog == DialogAction.SETPRO2) 
				  return defaultCloseDialog(env, 1, 2);
				}
			else if (targetId == 204100) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				}
			  else if (dialog == DialogAction.SET_SUCCEED) {
			  	removeQuestItem(env, 182204234, 1);
			  	giveQuestItem(env, 182204197, 1);
					return defaultCloseDialog(env, 2, 3, true, false);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204355) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					removeQuestItem(env, 182204197, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}