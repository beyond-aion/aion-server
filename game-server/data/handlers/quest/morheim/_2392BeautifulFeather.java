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
public class _2392BeautifulFeather extends AbstractQuestHandler {

	public _2392BeautifulFeather() {
		super(2392);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798085).addOnQuestStart(questId);
		qe.registerQuestNpc(798085).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798085) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798085) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (dialogActionId == SETPRO1) {
					if (player.getInventory().getItemCountByItemId(182204159) != 0) {
						removeQuestItem(env, 182204159, 1);
						changeQuestStep(env, 0, 1);
						qs.setRewardGroup(0);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					} else
						return sendQuestDialog(env, 1097);
				} else if (dialogActionId == SETPRO2) {
					if (player.getInventory().getItemCountByItemId(182204160) != 0) {
						removeQuestItem(env, 182204160, 1);
						changeQuestStep(env, 0, 2);
						qs.setRewardGroup(1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 6);
					} else
						return sendQuestDialog(env, 1097);
				} else if (dialogActionId == SETPRO3) {
					if (player.getInventory().getItemCountByItemId(182204161) != 0) {
						removeQuestItem(env, 182204161, 1);
						changeQuestStep(env, 0, 3);
						qs.setRewardGroup(2);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 7);
					} else
						return sendQuestDialog(env, 1097);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798085)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
