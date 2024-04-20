package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi, Shepper, Pad
 */
public class _1563TheLegendofVindachinerk extends AbstractQuestHandler {

	public _1563TheLegendofVindachinerk() {
		super(1563);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798096).addOnQuestStart(questId); // Poporinerk
		qe.registerQuestNpc(798096).addOnTalkEvent(questId); // Poporinerk
		qe.registerQuestNpc(279005).addOnTalkEvent(questId); // Kohrunerk
		qe.registerQuestItem(182201729, questId); // Jaiorunerk's Diary
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 798096) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START && var == 1) {
			switch (targetId) {
				case 798096: // Poporinerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO2:
							if (player.getInventory().getItemCountByItemId(182201729) < 1) {
								return sendQuestDialog(env, 1353);
							} else {
								player.getInventory().decreaseByItemId(182201729, 1);
								qs.setQuestVar(2);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 5);
							}
					}
					break;
				case 279005: // Kohrunerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1438);
						case SETPRO2:
							if (player.getInventory().getItemCountByItemId(182201729) < 1) {
								return sendQuestDialog(env, 1439);
							} else {
								player.getInventory().decreaseByItemId(182201729, 1);
								qs.setQuestVar(2);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 6);
							}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798096)
				qs.setRewardGroup(0);
			else if (targetId == 279005)
				qs.setRewardGroup(1);
			else
				return false;
			return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();

		if (id != 182201729)
			return HandlerResult.UNKNOWN;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return HandlerResult.UNKNOWN;
		changeQuestStep(env, 0, 1);

		return HandlerResult.SUCCESS;
	}
}
