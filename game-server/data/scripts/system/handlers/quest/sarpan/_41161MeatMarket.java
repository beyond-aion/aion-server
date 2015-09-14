package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _41161MeatMarket extends QuestHandler {

	private final static int questId = 41161;

	public _41161MeatMarket() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205583).addOnQuestStart(questId);
		qe.registerQuestNpc(205567).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205583) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205567) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
					else if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 2375);
				} else if (dialog == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1);
				} else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM_SIMPLE) {
					return checkQuestItems(env, 1, 2, true, 5, 0);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205567) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
