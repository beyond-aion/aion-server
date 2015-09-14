package quest.tiamaranta;

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
public class _41537StonyHeartedAncestors extends QuestHandler {

	private final static int questId = 41537;

	public _41537StonyHeartedAncestors() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205916).addOnQuestStart(questId);
		qe.registerQuestNpc(205954).addOnTalkEvent(questId);
		qe.registerQuestNpc(701147).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205916) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 205954) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else if(dialog == DialogAction.SETPRO1) {
					giveQuestItem(env, 182212537, 1);
					return defaultCloseDialog(env, 0, 1);
				}
			}
			else if (targetId == 701147) {
				//spawn?
				if (var > 0 && var < 3)
					return useQuestObject(env, var, var + 1, false, true);
				else if (var == 3)
					return useQuestObject(env, 3, 3, true, true);
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205954) {
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
}