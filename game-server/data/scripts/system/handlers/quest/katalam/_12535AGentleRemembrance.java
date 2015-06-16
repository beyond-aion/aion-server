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
public class _12535AGentleRemembrance extends QuestHandler {

	private final static int questId = 12535;
	
	public _12535AGentleRemembrance() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerQuestNpc(801008).addOnQuestStart(questId);
		qe.registerQuestNpc(801008).addOnTalkEvent(questId);
		qe.registerQuestNpc(701738).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801008) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case QUEST_ACCEPT_SIMPLE: {
						return sendQuestStartDialog(env, 182213319, 1);
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 701738) {
				if (var < 2)
					return useQuestObject(env, var, var + 1, false, true);
				else {
					removeQuestItem(env, 182213319, 1);
					return useQuestObject(env, 2, 3, true, true);
				}
			}
		}						
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801008) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
