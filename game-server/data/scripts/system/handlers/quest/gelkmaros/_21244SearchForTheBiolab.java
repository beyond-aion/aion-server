package quest.gelkmaros;

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
public class _21244SearchForTheBiolab extends QuestHandler {

	private final static int questId = 21244;

	public _21244SearchForTheBiolab() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(799317).addOnQuestStart(questId);
		qe.registerQuestNpc(799318).addOnTalkEvent(questId);
		qe.registerQuestNpc(799320).addOnTalkEvent(questId);
		qe.registerQuestNpc(799317).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 799317) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799318) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if(qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				}
				else if (dialog == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				}
			}
			else if (targetId == 799320) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if(qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1693);
				}
				else if (dialog == DialogAction.SETPRO2) {
					qs.setQuestVar(2);
					giveQuestItem(env, 182207924, 1);
					return defaultCloseDialog(env, 2, 2, true, false);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799317) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				removeQuestItem(env, 182207924, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
