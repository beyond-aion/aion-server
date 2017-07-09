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
public class _25032AvengeVarnur extends AbstractQuestHandler {

	public _25032AvengeVarnur() {
		super(25032);
	}

	@Override
	public void register() {
		// Muwel 804912
		// Redelf 804913
		// Grandung 804914
		qe.registerQuestNpc(804912).addOnQuestStart(questId);
		int[] npcs = { 804912, 804913, 804914 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(220057).addOnKillEvent(questId); // Drinbelin
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 804912) { // Muwel
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);

			switch (targetId) {
				case 804912: // Muwel
					if (var == 1) {
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1);
					}
					break;
				case 804913: // Redelf
					if (var == 2) {
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialogActionId == SET_SUCCEED) {
							qs.setQuestVar(var + 1);
							return defaultCloseDialog(env, var + 1, var + 1, true, false);
						}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 804914: // Grandung
					if (dialogActionId == USE_OBJECT)
						return sendQuestDialog(env, 10002);

					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		switch (targetId) {
			case 220057:
				if (var == 0) {
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}
}
