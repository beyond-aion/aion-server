package quest.inggison;

import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Luzien
 */
public class _11118MakingSetzkikiLaugh extends QuestHandler {

	private final static int questId = 11118;
	private final static int[] npc_ids = { 798985, 798963, 798986 };

	public _11118MakingSetzkikiLaugh() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798985).addOnQuestStart(questId);
		for (int npc_id : npc_ids) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (sendQuestNoneDialog(env, 798985, 4762))
			return true;
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int targetId = env.getTargetId();
		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798963) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else if (var == 1)
							return sendQuestDialog(env, 1352);
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 1, 2, false, 10000, 10001, 182206795, 1);
				}
			} else if (targetId == 798984) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 2)
							return sendQuestDialog(env, 2034);
						return false;
					case SET_SUCCEED:
						return defaultCloseDialog(env, 2, 3, true, false);
				}
			}
		}
		return sendQuestRewardDialog(env, 798985, 10002);
	}
}
