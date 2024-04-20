package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Enomine, Artur
 */
public class _80320ForTheAsmodians extends AbstractQuestHandler {

	private final static int[] npc_ids = { 831427 };

	public _80320ForTheAsmodians() {
		super(80320);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831427).addOnQuestStart(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		if (qs == null || qs.isStartable()) {
			if (targetId == 831427) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 831427:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case ASK_QUEST_ACCEPT:
							return sendQuestDialog(env, 4);
						case QUEST_ACCEPT_1:
							qs.setQuestVar(1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
			}
			if (player.getInventory().getItemCountByItemId(182215303) > 11) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831427) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
