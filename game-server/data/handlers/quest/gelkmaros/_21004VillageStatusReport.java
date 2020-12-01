package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class _21004VillageStatusReport extends AbstractQuestHandler {

	public _21004VillageStatusReport() {
		super(21004);
	}

	@Override
	public void register() {
		int[] npcs = { 799227, 799268, 799269 };
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerQuestNpc(799227).addOnQuestStart(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (sendQuestNoneDialog(env, 799227))
			return true;

		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 799268) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (env.getTargetId() == 799269) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
						return false;
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2, false, false);
				}
			} else if (env.getTargetId() == 799227) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 2)
							return sendQuestDialog(env, 2375);
						return false;
					case SELECT_QUEST_REWARD:
						return defaultCloseDialog(env, 2, 2, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (env.getTargetId() == 799227)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
