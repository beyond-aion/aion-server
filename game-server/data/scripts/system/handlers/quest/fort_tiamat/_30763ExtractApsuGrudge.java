package quest.fort_tiamat;

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
public class _30763ExtractApsuGrudge extends QuestHandler {

	private final static int questId = 30763;

	public _30763ExtractApsuGrudge() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(205891).addOnQuestStart(questId);
		qe.registerQuestNpc(730701).addOnTalkEvent(questId);
		qe.registerQuestNpc(205987).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205891) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182213270, 1);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 730701) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				}
				else if (dialog == DialogAction.SETPRO1) {
					giveQuestItem(env, 182213271, 1);
					removeQuestItem(env, 182213270, 1);
					return defaultCloseDialog(env, 0, 1);
				}
			}
			else if (targetId == 205987) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 2375);
				}
				else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					removeQuestItem(env, 182213271, 1);
					return defaultCloseDialog(env, 1, 1, true, true);
				}
			}
		}	
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205987) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 2375);
					}
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
}

