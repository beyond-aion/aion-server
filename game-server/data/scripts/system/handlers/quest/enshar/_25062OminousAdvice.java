package quest.enshar;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @Author Majka
 */
public class _25062OminousAdvice extends QuestHandler {

	private final static int questId = 25062;

	public _25062OminousAdvice() {
		super(questId);
	}

	@Override
	public void register() {
		// Runlil 804917
		// Sorg 804918
		qe.registerQuestNpc(804917).addOnQuestStart(questId);
		int[] npcs = { 804917, 804918 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.isStartable()) {
			if (targetId == 804917) { // Runlil
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 804917: // Runlil
					if (var == 0) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
							if (QuestService.collectItemCheck(env, true)) {
								qs.setStatus(QuestStatus.REWARD);
								qs.setQuestVar(var + 1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 804918: // Sorg
					if (dialog == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 10002);

					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
