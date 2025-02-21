package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author sky123
 */
public class _4907LepharistsinElysea extends AbstractQuestHandler {
	
	public _4907LepharistsinElysea() {
		super(4907);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204208).addOnQuestStart(questId);
		qe.registerQuestNpc(204208).addOnTalkEvent(questId);
		qe.registerQuestNpc(700511).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204208) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			long collect1 = player.getInventory().getItemCountByItemId(182207079);
			long collect2 = player.getInventory().getItemCountByItemId(182207080);
			long collect3 = player.getInventory().getItemCountByItemId(182207081);
			if (targetId == 700511) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 1011);
					case SETPRO1:
						changeQuestStep(env, 0, 1);
						return closeDialogWindow(env);
				}
			} else if (targetId == 204208) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1352);
					case CHECK_USER_HAS_QUEST_ITEM:
						if (collect1 >= 1 && collect2 >= 1 && collect3 >= 1)
							return defaultCloseDialog(env, 1, 1, true, true);
						else
							return sendQuestDialog(env, 10001);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
				if (targetId == 204208) {
					removeQuestItem(env, 182207079, 1);
					removeQuestItem(env, 182207080, 1);
					removeQuestItem(env, 182207081, 1);
					return sendQuestEndDialog(env);
				}
		}
		return false;
	}
}
