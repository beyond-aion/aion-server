package quest.enshar;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 */
public class _25031TheTejhiGhost extends QuestHandler {

	private final static int questId = 25031;

	public _25031TheTejhiGhost() {
		super(questId);
	}

	@Override
	public void register() {
		// Sigmuel 804911
		// Muwel 804912
		// Redelf 804913
		qe.registerQuestNpc(804911).addOnQuestStart(questId);
		int[] npcs = { 804911, 804912, 804913 };
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

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 804911) { // Sigmuel
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 804911: // Sigmuel
					if (var == 0) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM)
							return checkQuestItems(env, var, var + 1, false, 10000, 10001, 182215714, 1);
					}
					if (var == 1) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialog == DialogAction.SETPRO2)
							return defaultCloseDialog(env, var, var + 1, 0, 0, 182215714, 1);
					}
					break;
				case 804913: // Redelf
					if (var == 2) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialog == DialogAction.SET_SUCCEED) {
							qs.setQuestVar(var + 1);
							return defaultCloseDialog(env, var + 1, var + 1, true, false);
						}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 804912: // Muwel
					if (dialog == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 10002);

					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
