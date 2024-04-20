package quest.miragent_holy_templar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Nanou, Gigi
 */
public class _3936DecorationsOfSanctum extends AbstractQuestHandler {

	public _3936DecorationsOfSanctum() {
		super(3936);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203710).addOnQuestStart(questId);// Dairos
		qe.registerQuestNpc(203710).addOnTalkEvent(questId);// Dairos
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		// Start to Dairos
		if (qs == null || qs.isStartable()) {
			if (targetId == 203710) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				// 1 - Report the result to Dairos.
				case 203710:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case CHECK_USER_HAS_QUEST_ITEM:
							long itemCount1 = player.getInventory().getItemCountByItemId(182206091);
							long itemCount2 = player.getInventory().getItemCountByItemId(182206092);
							long itemCount3 = player.getInventory().getItemCountByItemId(182206093);
							long itemCount4 = player.getInventory().getItemCountByItemId(182206094);
							if (itemCount1 >= 10 && itemCount2 >= 10 && itemCount3 >= 10 && itemCount4 >= 10) {
								removeQuestItem(env, 182206091, 10);
								removeQuestItem(env, 182206092, 10);
								removeQuestItem(env, 182206093, 10);
								removeQuestItem(env, 182206094, 10);
								changeQuestStep(env, 1, 1, true);
								return sendQuestDialog(env, 5);
							} else
								return sendQuestDialog(env, 10001);
					}
					break;
				// No match
				default:
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203710)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
