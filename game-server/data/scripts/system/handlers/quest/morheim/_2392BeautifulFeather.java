package quest.morheim;

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
public class _2392BeautifulFeather extends QuestHandler {

	private final static int questId = 2392;
	
	
	public _2392BeautifulFeather() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(798085).addOnQuestStart(questId);
		qe.registerQuestNpc(798085).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798085) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 798085) { 
				if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
				}
			  else if (dialog == DialogAction.SETPRO1) {
			  	if(player.getInventory().getItemCountByItemId(182204159) != 0) {
			  		removeQuestItem(env, 182204159, 1);
			  		changeQuestStep(env, 0, 1, false);
				  	qs.setStatus(QuestStatus.REWARD);
				  	updateQuestStatus(env);
				  	return sendQuestDialog(env, 5);
			  	}
			  	else
			  		return sendQuestDialog(env, 1097);
			  }
			  else if (dialog == DialogAction.SETPRO2) {
			  	if(player.getInventory().getItemCountByItemId(182204160) != 0) {
			  		removeQuestItem(env, 182204160, 1);
			  		changeQuestStep(env, 0, 2, false);
				  	qs.setStatus(QuestStatus.REWARD);
				  	updateQuestStatus(env);
				  	return sendQuestDialog(env, 6);
			  	}
			  	else
			  		return sendQuestDialog(env, 1097);
			  }
			  else if (dialog == DialogAction.SETPRO3) {
			  	if(player.getInventory().getItemCountByItemId(182204161) != 0) {
			  		removeQuestItem(env, 182204161, 1);
			  		changeQuestStep(env, 0, 3, false);
				  	qs.setStatus(QuestStatus.REWARD);
				  	updateQuestStatus(env);
				  	return sendQuestDialog(env, 7);
			  	}
			  	else
			  		return sendQuestDialog(env, 1097);
			  }
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798085) {
				int rewInd = 0;
				if(qs.getQuestVarById(0) == 2)
					rewInd = 1;
				else if(qs.getQuestVarById(0) == 3)
					rewInd = 2;
				return sendQuestEndDialog(env, rewInd);
			}
		}
		return false;
	}
}
			 
	
			  	