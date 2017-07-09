package quest.the_eternal_bastion;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _28035TreacherousBeritra extends AbstractQuestHandler {

	public _28035TreacherousBeritra() {
		super(28035);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182213484, questId);
		qe.registerQuestNpc(801047).addOnTalkEvent(questId);
		qe.registerQuestNpc(800529).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				switch (dialogActionId) {
					case QUEST_ACCEPT_SIMPLE:
						QuestService.startQuest(env);
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 801047:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SELECT2_1:
							return sendQuestDialog(env, 1353);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					return false;
				case 800529:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD:
							removeQuestItem(env, 182213484, 1);
							changeQuestStep(env, 1, 1, true);
							return sendQuestDialog(env, 5);
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800529) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 1011));
		}
		return HandlerResult.FAILED;
	}
}
