package quest.enshar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @Author Majka
 */
public class _25073NoRevivalForTheBalaur extends AbstractQuestHandler {

	public _25073NoRevivalForTheBalaur() {
		super(25073);
	}

	@Override
	public void register() {
		// Drak tribe's heart 731556
		// Sorg 804918
		// Cenute 804732
		qe.registerQuestNpc(804918).addOnQuestStart(questId);
		int[] npcs = { 731556, 804918, 804732 };
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
			if (targetId == 804918) { // Sorg
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 731556: // Drak tribe's heart
					if (var == 0) {
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1011);
						}

						if (dialogActionId == SET_SUCCEED) {
							if (QuestService.collectItemCheck(env, true)) {
								qs.setQuestVar(var + 1);
								return defaultCloseDialog(env, var + 1, var + 1, true, false);
							}
							return closeDialogWindow(env);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 804732: // Cenute
					if (dialogActionId == USE_OBJECT)
						return sendQuestDialog(env, 10002);

					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
