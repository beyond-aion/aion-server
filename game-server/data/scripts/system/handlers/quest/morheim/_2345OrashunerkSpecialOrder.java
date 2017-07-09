package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _2345OrashunerkSpecialOrder extends AbstractQuestHandler {

	public _2345OrashunerkSpecialOrder() {
		super(2345);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798084).addOnQuestStart(questId);
		qe.registerQuestNpc(798084).addOnTalkEvent(questId);
		qe.registerQuestNpc(700238).addOnTalkEvent(questId);
		qe.registerQuestNpc(204304).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798084) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 798084) {
				if (dialogActionId == QUEST_SELECT) {
					if (var == 0)
						return sendQuestDialog(env, 1011);
					else if (var == 1)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 0, 1, false, 10000, 10001);
				} else if (dialogActionId == SETPRO10) {
					giveQuestItem(env, 182204137, 1);
					changeQuestStep(env, 1, 10);
					qs.setRewardGroup(0);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				} else if (dialogActionId == SETPRO20) {
					giveQuestItem(env, 182204138, 1);
					changeQuestStep(env, 1, 20);
					qs.setRewardGroup(1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return closeDialogWindow(env);
				}
			} else if (targetId == 700238 && player.getInventory().getItemCountByItemId(182204136) < 3) {
				return true; // looting
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204304) {
				if (dialogActionId == USE_OBJECT) {
					if (qs.getQuestVarById(0) == 10) {
						removeQuestItem(env, 182204137, 1);
						return sendQuestDialog(env, 1693);
					} else if (qs.getQuestVarById(0) == 20) {
						removeQuestItem(env, 182204138, 1);
						return sendQuestDialog(env, 2034);
					}
				}
				return sendQuestEndDialog(env);
			}
		}

		return false;
	}
}
