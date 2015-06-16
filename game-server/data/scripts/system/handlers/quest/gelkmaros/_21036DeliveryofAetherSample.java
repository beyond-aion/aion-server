package quest.gelkmaros;

import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class _21036DeliveryofAetherSample extends QuestHandler {

	private final static int questId = 21036;

	public _21036DeliveryofAetherSample() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 799258, 799238, 798713, 799239 };
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerQuestNpc(799258).addOnQuestStart(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (sendQuestNoneDialog(env, 799258, 182207832, 1))
			return true;

		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 799238) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			}
			else if (env.getTargetId() == 798713) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2, true, false);
				}
			}
		}
		return sendQuestRewardDialog(env, 799239, 2375);
	}
}
