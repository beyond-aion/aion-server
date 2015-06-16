package quest.beluslan;

import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class _2630TheDutifulSpy extends QuestHandler {

	private final static int questId = 2630;

	public _2630TheDutifulSpy() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 204799, 204777 };
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerQuestNpc(204799).addOnQuestStart(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (sendQuestNoneDialog(env, 204799))
			return true;
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 204777) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1, true, false);
				}
			}
		}
		return sendQuestRewardDialog(env, 204799, 2375);
	}
}
