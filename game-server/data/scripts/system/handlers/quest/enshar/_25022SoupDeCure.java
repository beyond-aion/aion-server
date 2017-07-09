package quest.enshar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 */
public class _25022SoupDeCure extends AbstractQuestHandler {

	public _25022SoupDeCure() {
		super(25022);
	}

	@Override
	public void register() {
		// Swith 804908
		// Kronag 804724
		qe.registerQuestNpc(804908).addOnQuestStart(questId);
		int[] npcs = { 804908, 804724 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 804908) { // Swith
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 804908: // Swith
					if (var == 0) {
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM)
							return checkQuestItems(env, var, var + 1, false, 10000, 10001, 182215709, 1);
					}
					if (var == 1) {
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SET_SUCCEED) {
							qs.setQuestVar(var + 1);
							return defaultCloseDialog(env, var + 1, var + 1, true, false, 0, 0, 182215709, 1);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 804724: // Kronag
					if (dialogActionId == USE_OBJECT)
						return sendQuestDialog(env, 10002);

					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
