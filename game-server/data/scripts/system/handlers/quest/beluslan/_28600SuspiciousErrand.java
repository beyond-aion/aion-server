package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class _28600SuspiciousErrand extends AbstractQuestHandler {

	public _28600SuspiciousErrand() {
		super(28600);
	}

	@Override
	public void register() {
		int[] npcs = { 204702, 205233, 204254, 804607 };
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerQuestNpc(204702).addOnQuestStart(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (sendQuestNoneDialog(env, 204702, 182213004, 1))
			return true;
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 205233) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						else if (var == 2)
							return sendQuestDialog(env, 2034);
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
					case SETPRO3:
						qs.setQuestVarById(0, 3);
						return defaultCloseDialog(env, 3, 3, true, false, 182213005, 1, 182213004, 1);
				}
			} else if (env.getTargetId() == 804607) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
						return false;
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2, 182213004, 1);
				}
			}
		}
		return sendQuestRewardDialog(env, 204702, 2375);
	}
}
