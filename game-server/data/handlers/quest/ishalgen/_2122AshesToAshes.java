package quest.ishalgen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _2122AshesToAshes extends AbstractQuestHandler {

	private int[] npcs = { 203551, 700148, 730029 };

	public _2122AshesToAshes() {
		super(2122);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182203120, questId);
		qe.registerCanAct(getQuestId(), 700148);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					return sendQuestStartDialog(env);
				} else if (env.getDialogActionId() == QUEST_REFUSE_1) {
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203551) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SELECT1_1:
						removeQuestItem(env, 182203120, 1);
						return sendQuestDialog(env, 1012);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 730029) {
				switch (dialogActionId) {
					case USE_OBJECT:
						if (player.getInventory().getItemCountByItemId(182203133) >= 1)
							return sendQuestDialog(env, 1352);
						else
							return sendQuestDialog(env, 1693);
					case SELECT2_1:
						removeQuestItem(env, 182203133, 1);
						return sendQuestDialog(env, 1353);
					case FINISH_DIALOG:
						return closeDialogWindow(env);
					case SETPRO2:
						return defaultCloseDialog(env, 1, 1, true, false);
				}
			} else if (targetId == 700148) {
				return true; // just give quest drop on use
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203551) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
