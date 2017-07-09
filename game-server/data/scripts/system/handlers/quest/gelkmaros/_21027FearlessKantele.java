package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class _21027FearlessKantele extends AbstractQuestHandler {

	public _21027FearlessKantele() {
		super(21027);
	}

	@Override
	public void register() {
		int[] npcs = { 799254, 799255 };
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerQuestNpc(799254).addOnQuestStart(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (sendQuestNoneDialog(env, 799254, 4762))
			return true;

		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 799255) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else if (var == 1)
							return sendQuestDialog(env, 1352);
						return false;
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 1, 2, true, 10000, 10001);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			}
		}
		return sendQuestRewardDialog(env, 799254, 10002);
	}
}
