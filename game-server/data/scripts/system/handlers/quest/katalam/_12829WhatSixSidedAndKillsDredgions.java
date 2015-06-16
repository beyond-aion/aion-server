package quest.katalam;

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
public class _12829WhatSixSidedAndKillsDredgions extends QuestHandler {

	private final static int questId = 12829;

	public _12829WhatSixSidedAndKillsDredgions() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(801235).addOnQuestStart(questId);
		qe.registerQuestNpc(801235).addOnTalkEvent(questId);
		qe.registerQuestNpc(730778).addOnTalkEvent(questId);
		qe.registerQuestNpc(206318).addOnAtDistanceEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801235) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 730778) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				}
				else if (dialog == DialogAction.SET_SUCCEED) {
					return defaultCloseDialog(env, 1, 2, true, false);
				}
			}
		}	
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801235) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
  		int var = qs.getQuestVarById(0);
  		if(var == 0)
  			changeQuestStep(env, 0, 1, false);
  		return true;
  		
  	}
		return false;
	}
}

