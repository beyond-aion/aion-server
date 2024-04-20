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
public class _80318TroubledArabella extends AbstractQuestHandler {

	private final static int[] npc_ids = { 831425, 831426 };

	public _80318TroubledArabella() {
		super(80318);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831425).addOnQuestStart(questId);
		qe.registerQuestItem(182215301, questId);
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
			if (targetId == 831425) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 831425:// Arabella
					switch (dialogActionId) {
						case USE_OBJECT:
							return sendQuestDialog(env, 1011);
						case ASK_QUEST_ACCEPT:
							return sendQuestDialog(env, 4);
						case QUEST_ACCEPT_1:
							if (!giveQuestItem(env, 182215301, 1))
								return true;
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831426) {// ziba
				switch (dialogActionId) {
					case USE_OBJECT:
						if (player.getInventory().getItemCountByItemId(182215301) == 1) {
							removeQuestItem(env, 182215301, 1);
							return sendQuestDialog(env, 1352);
						}
						return false;
					case SELECT2_1:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
