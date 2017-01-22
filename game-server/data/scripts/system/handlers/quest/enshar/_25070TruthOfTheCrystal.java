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
public class _25070TruthOfTheCrystal extends QuestHandler {

	private final static int questId = 25070;

	public _25070TruthOfTheCrystal() {
		super(questId);
	}

	@Override
	public void register() {
		// Brandun 804919
		qe.registerQuestNpc(804919).addOnQuestStart(questId);
		int[] npcs = { 804919 };
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
			if (targetId == 804919) { // Brandun
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 731552: // Cold crystal
					if (var == 0) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 1011);
						}

						if (dialog == DialogAction.SET_SUCCEED) {
							giveQuestItem(env, 182215723, 1);
							qs.setQuestVar(var + 1);
							return defaultCloseDialog(env, var + 1, var + 1, true, false);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 804919: // Brandun
					if (dialog == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 10002);

					removeQuestItem(env, 182215723, 1);
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
